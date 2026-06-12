package vn.elca.training.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import vn.elca.training.model.dto.response.ProjectDto;
import vn.elca.training.model.entity.Project;

@Mapper
public interface ProjectMapper {
    ProjectMapper INSTANCE = Mappers.getMapper(ProjectMapper.class);

    ProjectDto toProjectDto(Project entity);
}
