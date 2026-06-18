package vn.elca.training.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.elca.training.mapper.GroupMapper;
import vn.elca.training.model.dto.response.GroupDto;
import vn.elca.training.service.GroupService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupController {
    private final GroupService groupService;
    private final GroupMapper groupMapper;

    public GroupController(GroupService groupService, GroupMapper groupMapper) {
        this.groupService = groupService;
        this.groupMapper = groupMapper;
    }

    @GetMapping
    public List<GroupDto> getGroups() {
        return groupService.findAll().stream()
                .map(groupMapper::toDto)
                .collect(Collectors.toList());
    }
}
