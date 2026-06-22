package vn.elca.training.service;

import vn.elca.training.model.dto.response.GroupDto;
import vn.elca.training.model.entity.Group;

import java.util.List;

public interface GroupService {
    Group findById(Long id);

    List<GroupDto> findAll();
}
