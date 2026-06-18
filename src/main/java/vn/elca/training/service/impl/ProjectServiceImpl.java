package vn.elca.training.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.mapper.ProjectMapper;
import vn.elca.training.model.dto.response.ProjectDto;
import vn.elca.training.model.dto.request.ProjectCreationRequest;
import vn.elca.training.model.dto.request.ProjectUpdateRequest;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author vlp
 *
 */
@Service
@Profile("!dummy | dev")
public class ProjectServiceImpl implements ProjectService {

    private static final String PROJECT_DEFAULT_SORT_BY = "projectNumber";
    private static final String PROJECT_DEFAULT_SORT_DIR = "ASC";

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);
    private final ProjectRepository projectRepository;

    private final GroupService groupService;
    private final EmployeeService employeeService;

    private final ProjectValidator projectValidator;

    public ProjectServiceImpl(ProjectRepository projectRepository, GroupService groupService, EmployeeService employeeService, ProjectValidator projectValidator) {
        this.projectRepository = projectRepository;
        this.groupService = groupService;
        this.employeeService = employeeService;
        this.projectValidator = projectValidator;
    }


    @Override
    public Page<Project> findAll() {
        Pageable pageable = PaginationUtil.buildPaginationWithCustomSorting(PROJECT_DEFAULT_SORT_BY, PROJECT_DEFAULT_SORT_DIR);
        return projectRepository.findAll(pageable);
    }

    @Override
    public long count() {
        return projectRepository.count();
    }

    @Override
    public Project findProjectById(final Long id) {

        return projectRepository.findProjectById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project updateProject(final Long id, final ProjectUpdateRequest updateRequest) {
        log.info("Update info for project with id: {}", id);

        // 1. validate and return a target updated project
        Project targetProject = projectValidator.validateUpdateProject(id, updateRequest);

        // 2. find target group and target employees
        Group group = groupService.findById(updateRequest.getGroupId());
        List<Employee> employees = employeeService.findEmployeesByVisas(updateRequest.getVisas());

        // 3. update project info
        targetProject.setName(updateRequest.getName());
        targetProject.setCustomer(updateRequest.getCustomer());
        targetProject.setStatus(updateRequest.getStatus());
        targetProject.setStartDate(updateRequest.getStartDate());
        targetProject.setEndDate(updateRequest.getEndDate());
        targetProject.setEmployees(employees);
        targetProject.setGroup(group);

        return projectRepository.save(targetProject);
    }

    @Override
    public Page<Project> findProjectsByCriteria(final String keyword, final Status status, final Pageable pageable) {
        return  projectRepository.findProjectsByCriteria(keyword, status, pageable);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectDto createProject(final ProjectCreationRequest request){
        // 1. validate project
        projectValidator.validateCreateProject(request);

        // 2. find the target group and target employees
        Group group = groupService.findById(request.getGroupId());
        List<Employee> employees = employeeService.findEmployeesByVisas(request.getVisas());

        // 3. create new project
        Project project = new Project();
        project.setProjectNumber(request.getProjectNumber());
        project.setName(request.getName());
        project.setCustomer(request.getCustomer());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setGroup(group);
        project.setEmployees(employees);

        Project savedProject = projectRepository.save(project);

        return ProjectMapper.INSTANCE.toProjectDto(savedProject);
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
        projectRepository.deleteAll(projects);
    }
}
