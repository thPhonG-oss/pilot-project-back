package vn.elca.training.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.elca.training.mapper.ProjectMapper;
import vn.elca.training.model.dto.PageResponse;
import vn.elca.training.model.dto.ProjectDto;
import vn.elca.training.model.dto.request.ProjectRequestDto;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.Status;
import vn.elca.training.service.ProjectService;
import vn.elca.training.util.PaginationUtil;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gtn
 *
 */
@RestController
@RequestMapping("api/v1/projects")
@Validated
public class ProjectController extends AbstractApplicationController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ProjectDto createProject(@Valid @RequestBody ProjectRequestDto request){
        return projectService.createProject(request);
    }

    @GetMapping
    public PageResponse<ProjectDto> findAllProjects(){
        Page<Project> projectPage = projectService.findAll();
        List<ProjectDto> content = projectPage.getContent()
                .stream()
                .map(ProjectMapper.INSTANCE::toProjectDto)
                .collect(Collectors.toList());

        return new PageResponse<>(
                projectPage.getNumber() + 1,
                projectPage.getNumberOfElements(),
                content,
                projectPage.getTotalPages(),
                projectPage.getTotalElements(),
                projectPage.isLast());
    }

    @GetMapping("/search")
    public PageResponse<ProjectDto> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size
            ) {

        Pageable pageable = PaginationUtil.buildCustomPaginatinWithPageAndSize(page, size);

        Page<Project> projectPage = projectService.findProjectsByCriteria(keyword, status, pageable);
        List<ProjectDto> content = projectPage.getContent()
                .stream()
                .map(ProjectMapper.INSTANCE::toProjectDto)
                .collect(Collectors.toList());

        return new PageResponse<>(
                projectPage.getNumber() + 1,
                projectPage.getNumberOfElements(),
                content,
                projectPage.getTotalPages(),
                projectPage.getTotalElements(),
                projectPage.isLast());
    }

    @GetMapping("/{id}")
    public ProjectDto findProjectById(@Min(value = 1L, message = "Project ID must be a positive integer") @PathVariable Long id) {
        return ProjectMapper.INSTANCE.toProjectDto(projectService.findProjectById(id));
    }

    @PutMapping("/{id}")
    public ProjectDto updateProject(
            @Min(value = 1L, message = "Project ID must be a positive integer") @PathVariable Long id,
            @Valid @RequestBody ProjectDto projectDto
    ){
        log.info("Project name: {}", projectDto.getName());
        return ProjectMapper.INSTANCE.toProjectDto(projectService.updateProject(id, projectDto));
    }
}
