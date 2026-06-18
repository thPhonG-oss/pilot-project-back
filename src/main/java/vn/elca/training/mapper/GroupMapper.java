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
    default void afterMapping(Group entity,@MappingTarget GroupDto dto) {
        String leaderName = entity.getLeader() == null ? null : entity.getLeader().getFirstName() +  " " + entity.getLeader().getLastName();
        dto.setLeaderName(leaderName);
    }
}
