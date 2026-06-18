package vn.elca.training.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.elca.training.model.dto.response.ProjectDto;
import vn.elca.training.model.entity.Project;

@Mapper(componentModel = "spring", uses = {EmployeeMapper.class, GroupMapper.class})
public interface ProjectMapper {

    @Mapping(target = "employeeDtos", source = "employees")
    @Mapping(target = "groupDto", source = "group")
    ProjectDto toProjectDto(Project entity);

    @Mapping(target = "employeeDtos", ignore = true)
    @Mapping(target = "groupDto", ignore = true)
    ProjectDto toProjectSummary(Project entity);
}
