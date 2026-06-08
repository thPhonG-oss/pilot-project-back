package vn.elca.training.repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.querydsl.jpa.impl.JPAQuery;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.ApplicationWebConfig;
import vn.elca.training.model.entity.*;

@ContextConfiguration(classes = {ApplicationWebConfig.class})
@RunWith(value=SpringRunner.class)
public class ProjectRepositoryTest {
    @PersistenceContext
    private EntityManager em;
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Test
    public void testCountAll() {
        projectRepository.save(new Project("KSTA", LocalDate.now()));
        projectRepository.save(new Project("LAGAPEO", LocalDate.now()));
        projectRepository.save(new Project("ZHQUEST", LocalDate.now()));
        projectRepository.save(new Project("SECUTIX", LocalDate.now()));
        Assert.assertEquals(9, projectRepository.count());
    }

    @Test
    public void testFindOneWithQueryDSL() {
        final String PROJECT_NAME = "KSTA";
        projectRepository.save(new Project(PROJECT_NAME, LocalDate.now()));
        Project project = new JPAQuery<Project>(em)
                .from(QProject.project)
                .where(QProject.project.name.eq(PROJECT_NAME))
                .fetchFirst();
        Assert.assertEquals(PROJECT_NAME, project.getName());
    }

    @Test
    public void testSaveOneProject(){
        String projectName = "TEST_PROJECT";
        LocalDate finishDate = LocalDate.now();

        Project project = new Project(projectName, finishDate);
        project.setCustomer("TEST_CUSTOMER");

        Project savedProject = projectRepository.save(project);

        Assert.assertNotNull(savedProject.getId());
        Assert.assertEquals(projectName, savedProject.getName());
        Assert.assertEquals(finishDate, savedProject.getFinishingDate());
        Assert.assertEquals("TEST_CUSTOMER", savedProject.getCustomer());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testSaveMultipleProjectsWithHierarchy() {
        // Create employees for different positions
        Employee qmvLeader = new Employee("QMV", "QMV", "Leader", LocalDate.now().minusYears(30));
        Employee hhh = new Employee("HHH", "HHH", "QualityAgent", LocalDate.now().minusYears(28));
        Employee hhn = new Employee("HHN", "HHN", "Developer", LocalDate.now().minusYears(25));
        Employee plh = new Employee("PLH", "PLH", "QualityAgent", LocalDate.now().minusYears(26));
        Employee hrl = new Employee("HRL", "HRL", "Developer", LocalDate.now().minusYears(24));
        Employee tbh = new Employee("TBH", "TBH", "QualityAgent", LocalDate.now().minusYears(27));
        Employee tdh = new Employee("TDH", "TDH", "Developer", LocalDate.now().minusYears(29));

        // Create the group with QMV as leader
        Group qmvGroup = new Group(qmvLeader);

        // Create multiple projects and associate them with the group
        Project kstaProject = new Project("KSTA", LocalDate.now());
        kstaProject.setCustomer("KSTA_CUSTOMER");
        kstaProject.setGroup(qmvGroup);

        Project lagapeoProject = new Project("LAGAPEO", LocalDate.now());
        lagapeoProject.setCustomer("LAGAPEO_CUSTOMER");
        lagapeoProject.setGroup(qmvGroup);

        Project zhquestProject = new Project("ZHQUEST", LocalDate.now());
        zhquestProject.setCustomer("ZHQUEST_CUSTOMER");
        zhquestProject.setGroup(qmvGroup);

        // Add projects to group (inverse side) so cascade from group to projects works
        qmvGroup.getProjects().add(kstaProject);
        qmvGroup.getProjects().add(lagapeoProject);
        qmvGroup.getProjects().add(zhquestProject);
        // Add employees to projects — Project.employees has cascade PERSIST, so employees will be persisted
        kstaProject.getEmployees().add(hhh);
        kstaProject.getEmployees().add(plh);

        lagapeoProject.getEmployees().add(hhn);
        lagapeoProject.getEmployees().add(hrl);

        zhquestProject.getEmployees().add(tbh);
        zhquestProject.getEmployees().add(tdh);

        // Persist the group -> cascading will persist leader, projects, and employees
        Group savedGroup = groupRepository.save(qmvGroup);

        Assert.assertNotNull("Group id should be generated", savedGroup.getId());
        // verify group leader persisted
        Assert.assertNotNull("Group leader should be persisted and have id",
                savedGroup.getLeader().getId());

        // Verify projects persisted
        Pageable page = PageRequest.of(0, 10);
        long totalMatching = projectRepository.findAllByNameContainingIgnoreCase("", page).getTotalElements();

        // There are initial projects inserted via data.sql (5), plus 3 we added here;
        // but because we added @Rollback(true), in this test context we only check local persistence:
        // To be robust, verify that each project we inserted can be found by name.
        Project fetchedKsta = projectRepository.findAllByNameContainingIgnoreCase("KSTA", page)
                .getContent().stream().filter(p -> "KSTA_CUSTOMER".equals(p.getCustomer())).findFirst().orElse(null);
        Assert.assertNotNull("KSTA project should be persisted and queryable", fetchedKsta);
        Assert.assertEquals("KSTA", fetchedKsta.getName());
        Assert.assertEquals("KSTA_CUSTOMER", fetchedKsta.getCustomer());

        // Check association back to group and leader visa value
        Assert.assertNotNull("Project should have group set", fetchedKsta.getGroup());
        Assert.assertNotNull("Group leader should be present", fetchedKsta.getGroup().getLeader());
        Assert.assertEquals("QMV", fetchedKsta.getGroup().getLeader().getVisa());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testDeleteProject() {
        // Create and save a project
        String projectName = "DELETE_TEST_PROJECT";
        Project project = new Project(projectName, LocalDate.now());
        project.setStatus(Status.NEW);
        project.setCustomer("DELETE_TEST_CUSTOMER");

        Project savedProject = projectRepository.save(project);
        Long projectId = savedProject.getId();

        Assert.assertNotNull("Project should be saved with id", projectId);
        Assert.assertTrue("Project should exist before deletion", projectRepository.existsById(projectId));

        // Delete the project
        projectRepository.deleteById(projectId);

        // Verify deletion
        Assert.assertFalse("Project should not exist after deletion", projectRepository.existsById(projectId));
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testQueryProjectsByNameUsingQueryDSL() {
        // Create and save test projects with different names
        String targetName = "QUERYDSL_TEST_PROJECT";
        Project project1 = new Project(targetName, LocalDate.now());
        project1.setStatus(Status.INP);
        project1.setCustomer("CUSTOMER_1");

        Project project2 = new Project("OTHER_PROJECT", LocalDate.now());
        project2.setStatus(Status.NEW);
        project2.setCustomer("CUSTOMER_2");

        projectRepository.save(project1);
        projectRepository.save(project2);

        // Query using QueryDSL for exact name match
        Project foundProject = new JPAQuery<Project>(em)
                .from(QProject.project)
                .where(QProject.project.name.eq(targetName))
                .fetchFirst();

        Assert.assertNotNull("Project with name should be found", foundProject);
        Assert.assertEquals(targetName, foundProject.getName());
        Assert.assertEquals(Status.INP, foundProject.getStatus());
        Assert.assertEquals("CUSTOMER_1", foundProject.getCustomer());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testQueryProjectsByNameAndStatusUsingQueryDSL() {
        // Create and save projects with different combinations
        Project project1 = new Project("COMBINED_TEST_1", LocalDate.now());
        project1.setStatus(Status.INP);
        project1.setCustomer("CUST_1");

        Project project2 = new Project("COMBINED_TEST_1", LocalDate.now().minusDays(1));
        project2.setStatus(Status.NEW);
        project2.setCustomer("CUST_2");

        Project project3 = new Project("COMBINED_TEST_2", LocalDate.now());
        project3.setStatus(Status.INP);
        project3.setCustomer("CUST_3");

        projectRepository.save(project1);
        projectRepository.save(project2);
        projectRepository.save(project3);

        // Query: Find projects with name "COMBINED_TEST_1" AND status INP
        List<Project> results = new JPAQuery<Project>(em)
                .from(QProject.project)
                .where(QProject.project.name.eq("COMBINED_TEST_1")
                        .and(QProject.project.status.eq(Status.INP)))
                .fetch();

        Assert.assertTrue("Should find at least one matching project", results.size() >= 1);
        Assert.assertTrue("All results should match name and status",
                results.stream().allMatch(p ->
                        "COMBINED_TEST_1".equals(p.getName()) && Status.INP.equals(p.getStatus())));

        // Verify first result
        Project found = results.get(0);
        Assert.assertEquals("COMBINED_TEST_1", found.getName());
        Assert.assertEquals(Status.INP, found.getStatus());
    }
}
