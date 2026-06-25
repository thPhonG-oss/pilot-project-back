package vn.elca.training.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.elca.training.model.dto.request.ProjectDeleteRequest;
import vn.elca.training.model.dto.response.PageResponse;
import vn.elca.training.model.dto.response.ProjectDto;
import vn.elca.training.model.dto.request.ProjectCreationRequest;
import vn.elca.training.model.dto.request.ProjectUpdateRequest;
import vn.elca.training.model.entity.Status;
import vn.elca.training.service.ProjectService;
import vn.elca.training.util.PaginationUtil;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * @author gtn
 *
 */
@RestController
@RequestMapping("/api/v1/projects")
@Validated
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDto createProject(@Valid @RequestBody ProjectCreationRequest request){
        return projectService.createProject(request);
    }

    @GetMapping
    public PageResponse<ProjectDto> findAllProjects(
            @RequestParam(required = false, defaultValue = "1") final int page,
            @RequestParam(required = false, defaultValue = "10") final int size
    ) {
        Pageable pageable = PaginationUtil.buildFullCustomPagination(
                page,
                size,
                PaginationUtil.PROJECT_LIST_SORT_FIELD,
                "ASC"
        );
        return toPageResponse(projectService.findAll(pageable));
    }

    @GetMapping("/search")
    public PageResponse<ProjectDto> search(
            @RequestParam(required = false)
            @Size(max = 50, message = "{search.keyword.max-length}")
            final String keyword,
            @RequestParam(required = false) final Status status,
            @RequestParam(required = false, defaultValue = "1") final int page,
            @RequestParam(required = false, defaultValue = "10") final int size
            ) {

        Pageable pageable = PaginationUtil.buildCustomPaginationWithPageAndSize(page, size);
        return toPageResponse(projectService.findProjectsByCriteria(keyword, status, pageable));
    }

    @GetMapping("/{id}")
    public ProjectDto findProjectById(@Min(value = 1L, message = "{project.id.positive}") @PathVariable final Long id) {
        return projectService.findProjectById(id);
    }

    @PutMapping("/{id}")
    public ProjectDto updateProject(
            @Min(value = 1L, message = "{project.number.positive}") @PathVariable final Long id,
            @RequestBody @Valid final ProjectUpdateRequest updateRequest
    ){
        return projectService.updateProject(id, updateRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(
            @Min(value = 1L, message = "{project.id.positive}") @PathVariable final Long id
    ) {
        projectService.deleteProject(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProjects(@Valid @RequestBody final ProjectDeleteRequest request) {
        projectService.deleteProjects(request.getIds());
    }

    private PageResponse<ProjectDto> toPageResponse(Page<ProjectDto> projectPage) {
        return new PageResponse<>(
                projectPage.getNumber() + 1,
                projectPage.getSize(),
                projectPage.getContent(),
                projectPage.getTotalPages(),
                projectPage.getTotalElements(),
                projectPage.isLast());
    }
}
