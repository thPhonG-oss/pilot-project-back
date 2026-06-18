package vn.elca.training.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import vn.elca.training.model.dto.response.GroupDto;
import vn.elca.training.model.entity.Group;

@Mapper
public interface GroupMapper {
    GroupMapper INSTANCE = Mappers.getMapper(GroupMapper.class);
    @Mapping(target = "leaderVisa", source = "leader.visa")
    GroupDto toDto(Group entity);

    @AfterMapping
    default void afterMapping(Group entity,@MappingTarget GroupDto dto) {
        String leaderName = entity.getLeader().getLastName() + " " + entity.getLeader().getFirstName();
        dto.setLeaderName(leaderName == null ? null : leaderName);
    }
}
