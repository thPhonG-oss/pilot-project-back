package vn.elca.training.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.model.dto.ProjectDto;
import vn.elca.training.model.dto.request.ProjectCreationRequest;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.Status;

/**
 * @author vlp
 *
 */
public interface ProjectService {

    Page<Project> findAll();

    long count();

    Page<Project> findAllProjectsContainingIgnoreCase(String keyword);

    Project findProjectById(Long id);

    Project updateProject(Long id, ProjectDto projectDto);

    @Transactional(rollbackFor = Exception.class)
    Project createMaintennanceProject(Long oldProjectId);

    Page<Project> findProjectsByCriteria(String keyword, Status status, Pageable pageable);

    ProjectDto createProject(ProjectCreationRequest request);
}
