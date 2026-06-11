package vn.elca.training.service;

import vn.elca.training.model.entity.Group;

import java.util.Optional;

public interface GroupService {
    Group findById(Long id);
}
