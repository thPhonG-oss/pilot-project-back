package vn.elca.training.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import vn.elca.training.mapper.GroupMapper;
import vn.elca.training.model.dto.response.GroupDto;
import vn.elca.training.model.entity.Employee;
import vn.elca.training.model.entity.Group;
import vn.elca.training.model.exception.BusinessException;
import vn.elca.training.model.exception.ErrorCode;
import vn.elca.training.repository.GroupRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupServiceImpl Unit Tests")
class GroupServiceImplTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMapper groupMapper;

    @InjectMocks
    private GroupServiceImpl groupService;

    private Group testGroup;
    private GroupDto testGroupDto;

    @BeforeEach
    void setUp() {
        Employee leader = new Employee("QMV", "Quy", "Van", LocalDate.of(1990, 2, 3));
        ReflectionTestUtils.setField(leader, "id", 1L);

        testGroup = new Group();
        testGroup.setLeader(leader);
        ReflectionTestUtils.setField(testGroup, "id", 10L);

        testGroupDto = new GroupDto(10L, 1L, "QMV", "Quy Van");
    }

    @Test
    @DisplayName("Should return group when valid ID is provided")
    void findById_shouldReturnGroup_whenValidIdProvided() {
        // Arrange
        when(groupRepository.findById(10L)).thenReturn(Optional.of(testGroup));

        // Act
        Group result = groupService.findById(10L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getLeader().getVisa()).isEqualTo("QMV");
        verify(groupRepository, times(1)).findById(10L);
    }

    @Test
    @DisplayName("Should throw BusinessException when group does not exist")
    void findById_shouldThrowBusinessException_whenGroupNotFound() {
        // Arrange
        when(groupRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> groupService.findById(99L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.GROUP_NOT_FOUND));

        verify(groupRepository).findById(99L);
    }

    @Test
    @DisplayName("Should return mapped group DTOs sorted by id ascending")
    void findAll_shouldReturnMappedDtos_whenGroupsExist() {
        // Arrange
        Group secondGroup = new Group();
        ReflectionTestUtils.setField(secondGroup, "id", 20L);

        when(groupRepository.findAll(any(Sort.class))).thenReturn(Arrays.asList(testGroup, secondGroup));
        when(groupMapper.toDto(testGroup)).thenReturn(testGroupDto);
        when(groupMapper.toDto(secondGroup)).thenReturn(new GroupDto(20L, 1L, "HNH", "Hanh Ho"));

        // Act
        List<GroupDto> result = groupService.findAll();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getLeaderVisa()).isEqualTo("QMV");
        assertThat(result.get(1).getId()).isEqualTo(20L);

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(groupRepository).findAll(sortCaptor.capture());
        assertThat(sortCaptor.getValue().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.ASC);

        verify(groupMapper).toDto(testGroup);
        verify(groupMapper).toDto(secondGroup);
    }

    @Test
    @DisplayName("Should return empty list when no groups exist")
    void findAll_shouldReturnEmptyList_whenNoGroupsExist() {
        // Arrange
        when(groupRepository.findAll(any(Sort.class))).thenReturn(Collections.emptyList());

        // Act
        List<GroupDto> result = groupService.findAll();

        // Assert
        assertThat(result).isEmpty();
        verify(groupRepository).findAll(any(Sort.class));
        verify(groupMapper, never()).toDto(any());
    }
}
