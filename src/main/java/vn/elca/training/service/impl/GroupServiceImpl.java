package vn.elca.training.service.impl;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.mapper.GroupMapper;
import vn.elca.training.model.dto.response.GroupDto;
import vn.elca.training.model.entity.Group;
import vn.elca.training.model.exception.BusinessException;
import vn.elca.training.model.exception.ErrorCode;
import vn.elca.training.repository.GroupRepository;
import vn.elca.training.service.GroupService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;

    public GroupServiceImpl(GroupRepository groupRepository, GroupMapper groupMapper) {
        this.groupRepository = groupRepository;
        this.groupMapper = groupMapper;
    }

    @Override
    public Group findById(final Long id){
        return groupRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));
    }

    @Cacheable(value = "groups")
    @Override
    @Transactional(readOnly = true)
    public List<GroupDto> findAll() {
        return groupRepository.findAll(Sort.by("id").ascending()).stream()
                .map(groupMapper::toDto)
                .collect(Collectors.toList());
    }
}
