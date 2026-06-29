package vn.elca.training.repository.custom;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.ApplicationWebConfig;
import vn.elca.training.model.dto.request.ProjectSearchCondition;
import vn.elca.training.model.entity.Employee;
import vn.elca.training.model.entity.Group;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.Status;
import vn.elca.training.repository.GroupRepository;
import vn.elca.training.repository.ProjectRepository;
import vn.elca.training.testutil.TestEntityFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exercises the QueryDSL filters in {@link ProjectRepositoryCustomImpl} against a real database,
 * since the leader (to-one) vs. member (many-to-many EXISTS) join strategies can only be verified
 * end-to-end: a wrong join would silently duplicate rows or miscount, not throw at compile time.
 */
@ContextConfiguration(classes = {ApplicationWebConfig.class})
@RunWith(value = SpringRunner.class)
public class ProjectRepositoryCustomImplTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private GroupRepository groupRepository;

    private static final Pageable DEFAULT_PAGE = PageRequest.of(0, 10, Sort.by("projectNumber").ascending());

    private Employee employee(String visa) {
        return new Employee(visa, visa, visa, LocalDate.of(1990, 1, 1));
    }

    private Group groupWithLeader(Employee leader) {
        Group group = new Group();
        group.setLeader(leader);
        return group;
    }

    private Project projectIn(Group group, String name, LocalDate startDate, LocalDate endDate, Status status, Employee... members) {
        Project project = TestEntityFactory.project(name, startDate, name + "_CUSTOMER");
        project.setEndDate(endDate);
        project.setStatus(status);
        project.setGroup(group);
        group.getProjects().add(project);
        for (Employee member : members) {
            project.getEmployees().add(member);
        }
        return project;
    }

    private ProjectSearchCondition condition() {
        return new ProjectSearchCondition();
    }

    private List<Long> idsOf(Page<Project> page) {
        return page.getContent().stream().map(Project::getId).collect(Collectors.toList());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void shouldFilterByLeaderVisa_withoutDuplicatingProjects() {
        Employee leaderA = employee("ZLA");
        Employee leaderB = employee("ZLB");
        Employee memberX = employee("ZMX");

        Group groupA = groupWithLeader(leaderA);
        Group groupB = groupWithLeader(leaderB);

        Project projectUnderA1 = projectIn(groupA, "LEADER_FILTER_A1", LocalDate.of(2026, 1, 10), null, Status.NEW, memberX);
        Project projectUnderA2 = projectIn(groupA, "LEADER_FILTER_A2", LocalDate.of(2025, 1, 1), null, Status.NEW);
        Project projectUnderB = projectIn(groupB, "LEADER_FILTER_B", LocalDate.of(2026, 3, 1), null, Status.INP, memberX);

        groupRepository.save(groupA);
        groupRepository.save(groupB);

        ProjectSearchCondition condition = condition();
        condition.setLeaderVisa("zla"); // lower-case to also assert case-insensitivity

        Page<Project> result = projectRepository.findProjectsByCriteria(condition, DEFAULT_PAGE);

        Assert.assertEquals(2L, result.getTotalElements());
        List<Long> ids = idsOf(result);
        Assert.assertTrue(ids.contains(projectUnderA1.getId()));
        Assert.assertTrue(ids.contains(projectUnderA2.getId()));
        Assert.assertFalse(ids.contains(projectUnderB.getId()));
    }

    @Test
    @Transactional
    @Rollback(true)
    public void shouldFilterByMemberVisa_withoutFanOutDuplicates() {
        Employee leaderA = employee("YLA");
        Employee memberX = employee("YMX");
        Employee memberY = employee("YMY");

        Group group = groupWithLeader(leaderA);

        // project with TWO members, one of which is the search target: must appear exactly once
        Project multiMemberProject = projectIn(group, "MEMBER_FILTER_MULTI", LocalDate.of(2026, 1, 10), null, Status.NEW, memberX, memberY);
        Project otherMemberProject = projectIn(group, "MEMBER_FILTER_OTHER", LocalDate.of(2026, 2, 1), null, Status.NEW, memberX);
        Project unrelatedProject = projectIn(group, "MEMBER_FILTER_NONE", LocalDate.of(2026, 3, 1), null, Status.NEW);

        groupRepository.save(group);

        ProjectSearchCondition condition = condition();
        condition.setMemberVisa("ymx");

        Page<Project> result = projectRepository.findProjectsByCriteria(condition, DEFAULT_PAGE);

        Assert.assertEquals(2L, result.getTotalElements());
        List<Long> ids = idsOf(result);
        Assert.assertEquals("project with 2 members must not be duplicated by the EXISTS join",
                2, ids.size());
        Assert.assertTrue(ids.contains(multiMemberProject.getId()));
        Assert.assertTrue(ids.contains(otherMemberProject.getId()));
        Assert.assertFalse(ids.contains(unrelatedProject.getId()));
    }

    @Test
    @Transactional
    @Rollback(true)
    public void shouldFilterByMemberVisa_matchingOnlyProjectsContainingThatMember() {
        Employee leaderA = employee("XLA");
        Employee memberX = employee("XMX");
        Employee memberY = employee("XMY");

        Group group = groupWithLeader(leaderA);
        Project projectWithY = projectIn(group, "MEMBER_FILTER_Y_ONLY", LocalDate.of(2026, 1, 10), null, Status.NEW, memberX, memberY);
        Project projectWithoutY = projectIn(group, "MEMBER_FILTER_NO_Y", LocalDate.of(2026, 2, 1), null, Status.NEW, memberX);

        groupRepository.save(group);

        ProjectSearchCondition condition = condition();
        condition.setMemberVisa("XMY");

        Page<Project> result = projectRepository.findProjectsByCriteria(condition, DEFAULT_PAGE);

        Assert.assertEquals(1L, result.getTotalElements());
        Assert.assertEquals(projectWithY.getId(), result.getContent().get(0).getId());
        Assert.assertNotEquals(projectWithoutY.getId(), result.getContent().get(0).getId());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void shouldFilterByStartDateRange() {
        Employee leaderA = employee("WLA");
        Group group = groupWithLeader(leaderA);

        Project inRange = projectIn(group, "START_RANGE_IN", LocalDate.of(2026, 6, 15), null, Status.NEW);
        Project beforeRange = projectIn(group, "START_RANGE_BEFORE", LocalDate.of(2025, 1, 1), null, Status.NEW);
        Project afterRange = projectIn(group, "START_RANGE_AFTER", LocalDate.of(2027, 1, 1), null, Status.NEW);

        groupRepository.save(group);

        ProjectSearchCondition condition = condition();
        condition.setStartDateFrom(LocalDate.of(2026, 1, 1));
        condition.setStartDateTo(LocalDate.of(2026, 12, 31));

        Page<Project> result = projectRepository.findProjectsByCriteria(condition, DEFAULT_PAGE);

        List<Long> ids = idsOf(result);
        Assert.assertTrue(ids.contains(inRange.getId()));
        Assert.assertFalse(ids.contains(beforeRange.getId()));
        Assert.assertFalse(ids.contains(afterRange.getId()));
    }

    @Test
    @Transactional
    @Rollback(true)
    public void shouldFilterByEndDateRange_excludingNullEndDates() {
        Employee leaderA = employee("VLA");
        Group group = groupWithLeader(leaderA);

        Project withEndDateInRange = projectIn(group, "END_RANGE_IN", LocalDate.of(2025, 1, 1), LocalDate.of(2026, 6, 15), Status.FIN);
        Project withNullEndDate = projectIn(group, "END_RANGE_NULL", LocalDate.of(2025, 1, 1), null, Status.NEW);

        groupRepository.save(group);

        ProjectSearchCondition condition = condition();
        condition.setEndDateFrom(LocalDate.of(2026, 1, 1));
        condition.setEndDateTo(LocalDate.of(2026, 12, 31));

        Page<Project> result = projectRepository.findProjectsByCriteria(condition, DEFAULT_PAGE);

        List<Long> ids = idsOf(result);
        Assert.assertTrue(ids.contains(withEndDateInRange.getId()));
        Assert.assertFalse("a null end date must not match a bounded end-date range", ids.contains(withNullEndDate.getId()));
    }

    @Test
    @Transactional
    @Rollback(true)
    public void shouldCombineLeaderAndMemberFilters() {
        Employee leaderA = employee("ULA");
        Employee leaderB = employee("ULB");
        Employee memberX = employee("UMX");

        Group groupA = groupWithLeader(leaderA);
        Group groupB = groupWithLeader(leaderB);

        Project matching = projectIn(groupA, "COMBINED_MATCH", LocalDate.of(2026, 1, 10), null, Status.NEW, memberX);
        Project wrongLeader = projectIn(groupB, "COMBINED_WRONG_LEADER", LocalDate.of(2026, 1, 10), null, Status.NEW, memberX);
        Project wrongMember = projectIn(groupA, "COMBINED_WRONG_MEMBER", LocalDate.of(2026, 1, 10), null, Status.NEW);

        groupRepository.save(groupA);
        groupRepository.save(groupB);

        ProjectSearchCondition condition = condition();
        condition.setLeaderVisa("ULA");
        condition.setMemberVisa("UMX");

        Page<Project> result = projectRepository.findProjectsByCriteria(condition, DEFAULT_PAGE);

        Assert.assertEquals(1L, result.getTotalElements());
        Assert.assertEquals(matching.getId(), result.getContent().get(0).getId());
        List<Long> ids = idsOf(result);
        Assert.assertFalse(ids.contains(wrongLeader.getId()));
        Assert.assertFalse(ids.contains(wrongMember.getId()));
    }
}
