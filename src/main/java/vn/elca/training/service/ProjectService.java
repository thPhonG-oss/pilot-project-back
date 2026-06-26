package vn.elca.training.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.elca.training.model.dto.request.ProjectCreationRequest;
import vn.elca.training.model.dto.request.ProjectUpdateRequest;
import vn.elca.training.model.dto.response.ProjectDto;
import vn.elca.training.model.entity.Status;

import javax.validation.Valid;
import java.util.List;

/**
 * @author vlp
 *
 */
public interface ProjectService {

    Page<ProjectDto> findAll(Pageable pageable);

    ProjectDto findProjectById(final Long id);

    void updateProject(final Long id, final @Valid ProjectUpdateRequest updateRequest);

    Page<ProjectDto> findProjectsByCriteria(final String keyword, final Status status, final Pageable pageable);

    ProjectDto createProject(final ProjectCreationRequest request);

    void deleteProject(final Long id);

    void deleteProjects(final List<Long> ids);
}
