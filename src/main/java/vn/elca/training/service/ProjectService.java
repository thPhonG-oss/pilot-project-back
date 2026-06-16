package vn.elca.training.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.model.dto.response.ProjectDto;
import vn.elca.training.model.dto.request.ProjectCreationRequest;
import vn.elca.training.model.dto.request.ProjectUpdateRequest;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.Status;

import javax.validation.Valid;

/**
 * @author vlp
 *
 */
public interface ProjectService {

    Page<Project> findAll();

    long count();

    Project findProjectById(final Long id);

    Project updateProject(final Long id, final @Valid ProjectUpdateRequest updateRequest);

    Page<Project> findProjectsByCriteria(final String keyword, final Status status, final Pageable pageable);

    ProjectDto createProject(final ProjectCreationRequest request);
}
