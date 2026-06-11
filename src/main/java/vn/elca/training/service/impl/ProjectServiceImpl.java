package vn.elca.training.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.mapper.ProjectMapper;
import vn.elca.training.model.dto.ProjectDto;
import vn.elca.training.model.dto.request.ProjectCreationRequest;
import vn.elca.training.model.dto.request.ProjectUpdateRequest;
import vn.elca.training.model.entity.Employee;
import vn.elca.training.model.entity.Group;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.Status;
import vn.elca.training.model.exception.BusinessException;
import vn.elca.training.model.exception.ErrorCode;
import vn.elca.training.model.exception.ProjectNotFoundException;
import vn.elca.training.repository.ProjectRepository;
import vn.elca.training.service.EmployeeService;
import vn.elca.training.service.GroupService;
import vn.elca.training.service.ProjectService;
import vn.elca.training.util.PaginationUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author vlp
 *
 */
@Service
@Profile("!dummy | dev")
public class ProjectServiceImpl implements ProjectService {
    @PersistenceContext
    private EntityManager em;

    private static final String PROJECT_DEFAULT_SORT_BY = "projectNumber";
    private static final String PROJECT_DEFAULT_SORT_DIR = "ASC";

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);
    private final ProjectRepository projectRepository;

    private final GroupService groupService;
    private final EmployeeService employeeService;

    public ProjectServiceImpl(ProjectRepository projectRepository, GroupService groupService, EmployeeService employeeService) {
        this.projectRepository = projectRepository;
        this.groupService = groupService;
        this.employeeService = employeeService;
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
    public Page<Project> findAllProjectsContainingIgnoreCase(String keyword) {

        Pageable defaultPageable = PaginationUtil.buildDefaultPagination();

        if(keyword == null || keyword.isEmpty()){
            return projectRepository.findAll(defaultPageable);
        }

        return projectRepository.findAllByNameContainingIgnoreCase(keyword, defaultPageable);
    }

    @Override
    public Project findProjectById(Long id) {

        return projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project updateProject(Long id, @Valid ProjectUpdateRequest updateRequest) {
        log.info("Update info for project with id: {}", id);

        Project targetProject = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        if(updateRequest.getEndDate() != null && !updateRequest.getStartDate().isBefore(updateRequest.getEndDate())){
            throw new BusinessException(ErrorCode.INVALID_END_DATE);
        }

        Group group = groupService.findById(updateRequest.getGroupId());
        List<Employee> employees = employeeService.findEmployeesByVisas(updateRequest.getVisas());

        targetProject.setName(updateRequest.getName());
        targetProject.setCustomer(updateRequest.getCustomer());
        targetProject.setStatus(updateRequest.getStatus());
        targetProject.setStartDate(updateRequest.getStartDate());
        targetProject.setEndDate(updateRequest.getEndDate());
        targetProject.setEmployees(employees);
        targetProject.setGroup(group);

        return projectRepository.save(targetProject);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Project createMaintennanceProject(Long oldProjectId){
        // check existing project
        Project oldProject = projectRepository.findById(oldProjectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + oldProjectId));

        // create new maintenance project
        Project newMaintennanceProject = new Project();
        newMaintennanceProject.setName(buildMaintenanceProjectName(oldProject));
        newMaintennanceProject.setCustomer(oldProject.getCustomer());
        Project savedProject =  projectRepository.save(newMaintennanceProject);

        projectRepository.save(oldProject);

        return savedProject;
    }

    @Override
    public Page<Project> findProjectsByCriteria(String keyword, Status status, Pageable pageable) {
        return  projectRepository.findProjectsByCriteria(keyword, status, pageable);
    }

    private String buildMaintenanceProjectName(Project oldProject){

        return oldProject.getName() + "Maint." + LocalDate.now().getYear();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectDto createProject(ProjectCreationRequest request){
        if (projectRepository.existsByProjectNumber(request.getProjectNumber())) {
            throw new BusinessException(ErrorCode.PROJECT_NUMBER_EXISTS);
        }

        if(request.getEndDate() != null && !request.getStartDate().isBefore(request.getEndDate())){
            throw new BusinessException(ErrorCode.INVALID_END_DATE);
        }

        Group group = groupService.findById(request.getGroupId());
        List<Employee> employees = employeeService.findEmployeesByVisas(request.getVisas());

        Project project = new Project();
        project.setProjectNumber(request.getProjectNumber());
        project.setName(request.getName());
        project.setCustomer(request.getCustomer());

        project.setStatus(Status.NEW);

        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setGroup(group);
        project.setEmployees(employees);

        Project savedProject = projectRepository.save(project);

        return ProjectMapper.INSTANCE.toProjectDto(savedProject);
    }

    private Map<Status, Integer> buildStatusOrder(){
        Map<Status, Integer> statusOrder = new HashMap<>();
        statusOrder.put(Status.NEW, 1);
        statusOrder.put(Status.PLA, 2);
        statusOrder.put(Status.INP, 3);
        statusOrder.put(Status.FIN, 4);

        return statusOrder;
    }

}
