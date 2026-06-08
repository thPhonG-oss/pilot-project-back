package vn.elca.training.service.impl;

import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.model.dto.ProjectDto;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.QProject;
import vn.elca.training.model.exception.ProjectNotFoundException;
import vn.elca.training.repository.ProjectRepository;
import vn.elca.training.service.ProjectService;
import vn.elca.training.util.PaginationUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

/**
 * @author vlp
 *
 */
@Service
@Profile("!dummy | dev")
public class ProjectServiceImpl implements ProjectService {
    @PersistenceContext
    private EntityManager em;

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);
    private ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public List<Project> findAll() {
        return projectRepository.findAll();
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
        targetProject.setFinishingDate(projectDto.getFinishingDate());
        targetProject.setCustomer(projectDto.getCustomer());

        return projectRepository.save(targetProject);
    }
}
