package vn.elca.training.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.elca.training.model.dto.response.GroupDto;
import vn.elca.training.model.entity.Group;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    @Mapping(target = "leaderVisa", source = "leader.visa")
    GroupDto toDto(Group entity);

    @AfterMapping
    default void afterMapping(Group entity, @MappingTarget GroupDto dto) {
        if (entity.getLeader() == null) {
            dto.setLeaderName(null);
            return;
        }
        dto.setLeaderName(formatLeaderName(
                entity.getLeader().getFirstName(),
                entity.getLeader().getLastName()
        ));
    }

    default String formatLeaderName(String firstName, String lastName) {
        boolean hasFirst = firstName != null && !firstName.trim().isEmpty();
        boolean hasLast = lastName != null && !lastName.trim().isEmpty();

        if (hasFirst && hasLast) {
            return firstName.trim() + " " + lastName.trim();
        }
        if (hasFirst) {
            return firstName.trim();
        }
        if (hasLast) {
            return lastName.trim();
        }
        return null;
    }
}
