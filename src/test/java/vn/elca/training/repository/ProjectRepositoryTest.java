package vn.elca.training.repository;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.querydsl.jpa.impl.JPAQuery;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.ApplicationWebConfig;
import vn.elca.training.model.entity.*;
import vn.elca.training.testutil.TestEntityFactory;

@ContextConfiguration(classes = {ApplicationWebConfig.class})
@RunWith(value=SpringRunner.class)
public class ProjectRepositoryTest {
    @PersistenceContext
    private EntityManager em;
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    public void testCountAll() {
        long initialCount = projectRepository.count();

        projectRepository.save(TestEntityFactory.project("COUNT_KSTA", LocalDate.now()));
        projectRepository.save(TestEntityFactory.project("COUNT_LAGAPEO", LocalDate.now()));
        projectRepository.save(TestEntityFactory.project("COUNT_ZHQUEST", LocalDate.now()));
        projectRepository.save(TestEntityFactory.project("COUNT_SECUTIX", LocalDate.now()));

        Assert.assertEquals(initialCount + 4, projectRepository.count());
    }

    @Test
    public void testFindOneWithQueryDSL() {
        final String PROJECT_NAME = "QUERYDSL_KSTA_TEST";
        projectRepository.save(TestEntityFactory.project(PROJECT_NAME, LocalDate.now()));
        Project project = new JPAQuery<Project>(em)
                .from(QProject.project)
                .where(QProject.project.name.eq(PROJECT_NAME))
                .fetchFirst();
        Assert.assertEquals(PROJECT_NAME, project.getName());
    }

    @Test
    public void testSaveOneProject(){
        String projectName = "TEST_PROJECT";
        LocalDate startDate = LocalDate.now();

        Project project = TestEntityFactory.project(projectName, startDate, "TEST_CUSTOMER");

        Project savedProject = projectRepository.save(project);

        Assert.assertNotNull(savedProject.getId());
        Assert.assertEquals(projectName, savedProject.getName());
        Assert.assertEquals(startDate, savedProject.getStartDate());
        Assert.assertEquals("TEST_CUSTOMER", savedProject.getCustomer());
        Assert.assertNotNull(savedProject.getProjectNumber());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testSaveMultipleProjectsWithHierarchy() {
        // Reuse seeded leader to avoid unique visa violation (QMV exists in data.sql)
        Employee qmvLeader = employeeRepository.findByVisa("QMV")
                .orElseThrow(() -> new IllegalStateException("Seed employee QMV not found"));

        Employee hhh = new Employee("HHH", "HHH", "QualityAgent", LocalDate.now().minusYears(28));
        Employee hhn = new Employee("HHN", "HHN", "Developer", LocalDate.now().minusYears(25));
        Employee plh = new Employee("PLH", "PLH", "QualityAgent", LocalDate.now().minusYears(26));
        Employee hrl = new Employee("HRL", "HRL", "Developer", LocalDate.now().minusYears(24));
        Employee tbh = new Employee("TBH", "TBH", "QualityAgent", LocalDate.now().minusYears(27));
        Employee tdh = new Employee("TDH", "TDH", "Developer", LocalDate.now().minusYears(29));

        // Create the group with QMV as leader
        Group qmvGroup = new Group();
        qmvGroup.setLeader(qmvLeader);

        // Create multiple projects and associate them with the group
        Project kstaProject = TestEntityFactory.project("KSTA", LocalDate.now(), "KSTA_CUSTOMER");
        kstaProject.setGroup(qmvGroup);

        Project lagapeoProject = TestEntityFactory.project("LAGAPEO", LocalDate.now(), "LAGAPEO_CUSTOMER");
        lagapeoProject.setGroup(qmvGroup);

        Project zhquestProject = TestEntityFactory.project("ZHQUEST", LocalDate.now(), "ZHQUEST_CUSTOMER");
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

        // Verify projects persisted using saved ids (avoid collisions with other tests/data.sql)
        Project fetchedKsta = projectRepository.findById(kstaProject.getId()).orElse(null);
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
        Project project = TestEntityFactory.project(projectName, LocalDate.now(), "DELETE_TEST_CUSTOMER");
        project.setStatus(Status.NEW);

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
        Project project1 = TestEntityFactory.project(targetName, LocalDate.now(), "CUSTOMER_1");
        project1.setStatus(Status.INP);

        Project project2 = TestEntityFactory.project("OTHER_PROJECT", LocalDate.now(), "CUSTOMER_2");
        project2.setStatus(Status.NEW);

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
        Project project1 = TestEntityFactory.project("COMBINED_TEST_1", LocalDate.now(), "CUST_1");
        project1.setStatus(Status.INP);

        Project project2 = TestEntityFactory.project("COMBINED_TEST_1", LocalDate.now().minusDays(1), "CUST_2");
        project2.setStatus(Status.NEW);

        Project project3 = TestEntityFactory.project("COMBINED_TEST_2", LocalDate.now(), "CUST_3");
        project3.setStatus(Status.INP);

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
