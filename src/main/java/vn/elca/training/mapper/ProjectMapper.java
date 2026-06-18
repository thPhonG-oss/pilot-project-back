package vn.elca.training.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import vn.elca.training.model.dto.response.ProjectDto;
import vn.elca.training.model.entity.Project;

@Mapper(uses = {EmployeeMapper.class, GroupMapper.class})
public interface ProjectMapper {
    ProjectMapper INSTANCE = Mappers.getMapper(ProjectMapper.class);

    @Mapping(target = "employeeDtos", source = "employees")
    @Mapping(target = "groupDto", source = "group")
    ProjectDto toProjectDto(Project entity);

    @Mapping(target = "employeeDtos", ignore = true)
    @Mapping(target = "groupDto", ignore = true)
    ProjectDto toProjectSummary(Project entity);
}
