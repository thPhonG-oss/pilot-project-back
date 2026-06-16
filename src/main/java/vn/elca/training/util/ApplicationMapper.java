package vn.elca.training.util;

import org.springframework.stereotype.Component;
import vn.elca.training.mapper.ProjectMapper;
import vn.elca.training.model.dto.response.EmployeeDto;
import vn.elca.training.model.dto.response.GroupDto;
import vn.elca.training.model.dto.response.ProjectDto;
import vn.elca.training.model.entity.Employee;
import vn.elca.training.model.entity.Group;
import vn.elca.training.model.entity.Project;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gtn
 */
@Component
public class ApplicationMapper {
    public ApplicationMapper() {
        // Mapper utility class
    }
    public GroupDto toDto(Group group) {
        Employee leader = group.getLeader();
        String leaderVisa = leader == null ? null : leader.getVisa();
        String leaderName = leader == null ? null : leader.getLastName() + " " + leader.getFirstName();

        return new GroupDto(group.getId(), leaderVisa, leaderName);
    }

    public ProjectDto toProjectDto(Project project){
        ProjectDto projectDto = ProjectMapper.INSTANCE.toProjectDto(project);
        List<EmployeeDto> employeeDtos = project.getEmployees().stream()
                .map(employee -> new EmployeeDto(employee.getId(), employee.getVisa(), employee.getFirstName(), employee.getLastName()))
                .collect(Collectors.toList());
        GroupDto groupDto = this.toDto(project.getGroup());

        projectDto.setEmployeeDtos(employeeDtos);
        projectDto.setGroupDto(groupDto);

        return projectDto;
    }
}
