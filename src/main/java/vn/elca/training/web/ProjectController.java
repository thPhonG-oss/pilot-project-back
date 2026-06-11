package vn.elca.training.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.elca.training.mapper.ProjectMapper;
import vn.elca.training.model.dto.PageResponse;
import vn.elca.training.model.dto.ProjectDto;
import vn.elca.training.model.entity.Project;
import vn.elca.training.service.ProjectService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gtn
 *
 */
@RestController
@RequestMapping("/projects")
@Validated
public class ProjectController extends AbstractApplicationController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);
    @Autowired
    private ProjectService projectService;

    @GetMapping("/search")
    public PageResponse<ProjectDto> search(
            @RequestParam(required = false) String keyword
    ) {
        Page<Project> projectPage = projectService.findAllProjectsContainingIgnoreCase(keyword);
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
        return mapper.projectToProjectDto(projectService.findProjectById(id));
    }

    @PutMapping("/{id}")
    public ProjectDto updateProject(
            @Min(value = 1L, message = "Project ID must be a positive integer") @PathVariable Long id,
            @Valid @RequestBody ProjectDto projectDto
    ){
        log.info("Project name: {}", projectDto.getName());
        return mapper.projectToProjectDto(projectService.updateProject(id, projectDto));
    }

    @PostMapping("/{oldProjectId}")
    public ProjectDto createMaintenanceProject(@Valid @PathVariable Long oldProjectId){
        return mapper.projectToProjectDto(projectService.createMaintennanceProject(oldProjectId));
    }
}
