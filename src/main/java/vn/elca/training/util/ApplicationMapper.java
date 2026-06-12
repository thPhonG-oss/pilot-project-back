package vn.elca.training.util;

import org.springframework.stereotype.Component;
import vn.elca.training.model.dto.response.GroupDto;
import vn.elca.training.model.entity.Employee;
import vn.elca.training.model.entity.Group;

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
}
