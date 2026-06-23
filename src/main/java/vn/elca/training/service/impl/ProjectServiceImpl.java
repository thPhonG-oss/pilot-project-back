package vn.elca.training.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.mapper.ProjectMapper;
import vn.elca.training.model.dto.request.ProjectCreationRequest;
import vn.elca.training.model.dto.request.ProjectUpdateRequest;
import vn.elca.training.model.dto.response.ProjectDto;
import vn.elca.training.model.entity.Employee;
import vn.elca.training.model.entity.Group;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.Status;
import vn.elca.training.model.exception.BusinessException;
import vn.elca.training.model.exception.ErrorCode;
import vn.elca.training.repository.ProjectRepository;
import vn.elca.training.service.EmployeeService;
import vn.elca.training.service.GroupService;
import vn.elca.training.service.ProjectService;
import vn.elca.training.util.PaginationUtil;
import vn.elca.training.validator.ProjectValidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author vlp
 *
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private static final String PROJECT_DEFAULT_SORT_BY = "projectNumber";
    private static final String PROJECT_DEFAULT_SORT_DIR = "ASC";

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);
    private final ProjectRepository projectRepository;

    private final GroupService groupService;
    private final EmployeeService employeeService;

    private final ProjectValidator projectValidator;
    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectRepository projectRepository, GroupService groupService, EmployeeService employeeService, ProjectValidator projectValidator, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.groupService = groupService;
        this.employeeService = employeeService;
        this.projectValidator = projectValidator;
        this.projectMapper = projectMapper;
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDto> findAll() {
        Pageable pageable = PaginationUtil.buildPaginationWithCustomSorting(PROJECT_DEFAULT_SORT_BY, PROJECT_DEFAULT_SORT_DIR);
        return projectRepository.findAll(pageable).map(projectMapper::toProjectSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDto findProjectById(final Long id) {
        Project project = projectRepository.findProjectById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        return projectMapper.toProjectDto(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectDto updateProject(final Long id, final ProjectUpdateRequest updateRequest) {
        log.info("Update info for project with id: {}", id);

        // 1. validate and return a target updated project
        Project targetProject = projectValidator.validateUpdateProject(id, updateRequest);

        // 2. find target group
        Group group = groupService.findById(updateRequest.getGroupId());

        // 3. clear employees and update new employees
        syncProjectEmployees(targetProject, updateRequest.getVisas());

        // 4. update project info
        targetProject.setName(updateRequest.getName());
        targetProject.setCustomer(updateRequest.getCustomer());
        targetProject.setStatus(updateRequest.getStatus());
        targetProject.setStartDate(updateRequest.getStartDate());
        targetProject.setEndDate(updateRequest.getEndDate());
        targetProject.assignGroup(group);

        log.info("Updated project id={}", id);
        return projectMapper.toProjectDto(targetProject);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDto> findProjectsByCriteria(final String keyword, final Status status, final Pageable pageable) {
        if(keyword == null || keyword.trim().isEmpty() && status == null) {
            return projectRepository.findAll(pageable).map(projectMapper::toProjectSummary);
        }

        return projectRepository.findProjectsByCriteria(keyword, status, pageable)
                .map(projectMapper::toProjectSummary);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectDto createProject(final ProjectCreationRequest request){
        log.info("Create project with number: {}", request.getProjectNumber());
        // 1. validate project
        projectValidator.validateCreateProject(request);

        // 2. find the target group and target employees
        Group group = groupService.findById(request.getGroupId());
        List<Employee> employees = request.getVisas().isEmpty() ? Collections.emptyList() :  employeeService.findEmployeesByVisas(request.getVisas());

        // 3. create new project
        Project project = new Project();
        project.setProjectNumber(request.getProjectNumber());
        project.setName(request.getName());
        project.setCustomer(request.getCustomer());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.assignGroup(group);
        employees.forEach(project::addEmployee);

        Project saved = projectRepository.save(project);
        log.info("Created project id={}, number={}", saved.getId(), saved.getProjectNumber());
        return projectMapper.toProjectSummary(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(final Long id) {
        log.info("Delete project with id: {}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        projectValidator.validateDeleteProject(project);
        projectRepository.delete(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProjects(final List<Long> ids) {
        List<Long> distinctIds = ids.stream().distinct().collect(Collectors.toList());
        log.info("Delete projects with ids: {}", distinctIds);

        List<Project> projects = projectRepository.findAllById(distinctIds);
        projectValidator.validateDeleteProjects(projects, distinctIds);
        projectRepository.deleteEmployeeLinksByProjectIds(distinctIds);
        projectRepository.deleteProjectsByIds(distinctIds);
    }

    private void syncProjectEmployees(Project targetProject, List<String> requestedVisas){
        Set<String> requested = toVisaSet(requestedVisas);

        // 1. empty/null request -> sync bi-directional
        if (requested.isEmpty()) {
            new ArrayList<>(targetProject.getEmployees())
                    .forEach(targetProject::removeEmployee);
            return;
        }

        Set<String> current = targetProject.getEmployees().stream()
                .map(e -> e.getVisa().toUpperCase()) // database is source of truth
                .collect(Collectors.toSet());

        if (current.equals(requested)) {
            return;
        }

        // 2. visa to delete
        new ArrayList<>(targetProject.getEmployees()).stream()
                .filter(employee -> !requested.contains(employee.getVisa().toUpperCase()))
                .forEach(targetProject::removeEmployee);

        // 3. visa to add
        List<String> visasToAdd = requested.stream()
                .filter(visa -> !current.contains(visa))
                .collect(Collectors.toList());

        // 4. add new employee(s)
        if (!visasToAdd.isEmpty()) {
            employeeService.findEmployeesByVisas(visasToAdd)
                    .forEach(targetProject::addEmployee);
        }
    }

    private Set<String> toVisaSet(List<String> visas) {
        if (visas == null || visas.isEmpty()) {
            return Collections.emptySet();
        }
        return visas.stream()
                .filter(v -> v != null && !v.trim().isEmpty())
                .map(v -> v.trim().toUpperCase())
                .collect(Collectors.toSet());
    }
}
