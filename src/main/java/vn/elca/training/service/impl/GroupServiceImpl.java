package vn.elca.training.service.impl;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.elca.training.model.entity.Group;
import vn.elca.training.model.exception.BusinessException;
import vn.elca.training.model.exception.ErrorCode;
import vn.elca.training.repository.GroupRepository;
import vn.elca.training.service.GroupService;

import java.util.List;

@Service
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;

    public GroupServiceImpl(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Override
    public Group findById(final Long id){
        return groupRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));
    }

    @Override
    public List<Group> findAll() {
        return groupRepository.findAll(Sort.by("id").ascending());
    }
}
