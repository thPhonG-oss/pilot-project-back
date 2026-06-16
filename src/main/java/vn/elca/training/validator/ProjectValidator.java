package vn.elca.training.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.elca.training.model.dto.request.ProjectCreationRequest;
import vn.elca.training.model.dto.request.ProjectUpdateRequest;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.exception.BusinessException;
import vn.elca.training.model.exception.ErrorCode;
import vn.elca.training.repository.ProjectRepository;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ProjectValidator {
    private final ProjectRepository projectRepository;

    public void validateCreateProject(ProjectCreationRequest request){
        validateProjectNumberExists(request.getProjectNumber());
        validateDateRange(request.getStartDate(), request.getEndDate());
    }

    public Project validateUpdateProject(Long id, ProjectUpdateRequest request){
        Project targetProject = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        validateDateRange(request.getStartDate(), request.getEndDate());

        return targetProject;
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
