package vn.elca.training.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.elca.training.model.dto.request.ProjectCreationRequest;
import vn.elca.training.model.dto.request.ProjectUpdateRequest;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.Status;
import vn.elca.training.model.exception.BusinessException;
import vn.elca.training.model.exception.ErrorCode;
import vn.elca.training.repository.ProjectRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProjectValidator {
    private final ProjectRepository projectRepository;

    public void validateCreateProject(ProjectCreationRequest request){
        validateProjectNumberExists(request.getProjectNumber());
        validateDateRange(request.getStartDate(), request.getEndDate());
    }

    public Project validateUpdateProject(Long id, ProjectUpdateRequest request){
        Project targetProject = projectRepository.findProjectById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        validateDateRange(request.getStartDate(), request.getEndDate());

        return targetProject;
    }

    public void validateDeleteProject(Project project) {
        validateDeletableStatus(Collections.singletonList(project));
    }

    public void validateDeleteProjects(List<Project> foundProjects, List<Long> requestedIds) {
        Set<Long> foundIds = foundProjects.stream()
                .map(Project::getId)
                .collect(Collectors.toSet());

        boolean hasMissingProject = requestedIds.stream().anyMatch(id -> !foundIds.contains(id));
        if (hasMissingProject) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }

        validateDeletableStatus(foundProjects);
    }

    private void validateDeletableStatus(List<Project> projects) {
        String notDeletableProjectNumbers = projects.stream()
                .filter(project -> !Status.NEW.equals(project.getStatus()))
                .map(project -> String.valueOf(project.getProjectNumber()))
                .collect(Collectors.joining(", "));

        if (!notDeletableProjectNumbers.isEmpty()) {
            throw new BusinessException(ErrorCode.PROJECT_DELETE_NOT_ALLOWED, notDeletableProjectNumbers);
        }
    }

    private void validateDateRange(LocalDate start, LocalDate end){
        if(end != null && end.isBefore(start))
            throw new BusinessException(ErrorCode.INVALID_END_DATE);
    }

    private void validateProjectNumberExists(Long projectNumber){
        if(projectRepository.existsByProjectNumber(projectNumber))
            throw new BusinessException(ErrorCode.PROJECT_NUMBER_EXISTS);
    }
}
