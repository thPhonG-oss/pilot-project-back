package vn.elca.training.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import vn.elca.training.model.dto.ProjectDto;
import vn.elca.training.model.entity.Project;

/**
 * @author vlp
 *
 */
public interface ProjectService {

    List<Project> findAll();

    long count();

    Page<Project> findAllProjectsContainingIgnoreCase(String keyword);

    Project findProjectById(Long id);

    Project updateProject(Long id, ProjectDto projectDto);
}
