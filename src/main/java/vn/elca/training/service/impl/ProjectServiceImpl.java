package vn.elca.training.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.model.dto.ProjectDto;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.Status;
import vn.elca.training.model.exception.ProjectNotFoundException;
import vn.elca.training.repository.ProjectRepository;
import vn.elca.training.service.ProjectService;
import vn.elca.training.util.PaginationUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;

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
    private ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
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
    public Project updateProject(Long id, ProjectDto projectDto) {
        log.info("Update info for project with id: {}", id);

        Project targetProject = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));

        targetProject.setName(projectDto.getName());
        targetProject.setCustomer(projectDto.getCustomer());

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
}
