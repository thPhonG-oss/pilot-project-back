package vn.elca.training.util;

import org.springframework.stereotype.Component;
import vn.elca.training.model.dto.response.ProjectDto;
import vn.elca.training.model.entity.Project;

/**
 * @author gtn
 */
@Component
public class ApplicationMapper {
    public ApplicationMapper() {
        // Mapper utility class
    }
    public ProjectDto projectToProjectDto(Project entity) {
        ProjectDto dto = new ProjectDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCustomer(entity.getCustomer());
        return dto;
    }
}
