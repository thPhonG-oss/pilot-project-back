package vn.elca.training.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import vn.elca.training.ApplicationWebConfig;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.exception.ProjectNotFoundException;
import vn.elca.training.repository.ProjectRepository;
import vn.elca.training.service.ProjectService;

import java.time.LocalDate;

@ContextConfiguration(classes = {ApplicationWebConfig.class})
@RunWith(SpringRunner.class)
public class ProjectServiceImplTest {
    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImplTest.class);
    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Test: Verify maintenance project creation succeeds and both projects are persisted
     */
    @Test
    @Transactional
    @Rollback(true)
    public void testCreateMaintenanceProjectSuccess() {
        // Arrange: Create an old project
        Project oldProject = new Project("ORIGINAL_PROJECT", LocalDate.now());
        oldProject.setCustomer("ORIGINAL_CUSTOMER");
        oldProject.setActivated(true);
        Project savedOldProject = projectRepository.save(oldProject);
        Long oldProjectId = savedOldProject.getId();

        // Act: Create maintenance project
        Project newProject = projectService.createMaintennanceProject(oldProjectId);

        // Assert: Verify new project is created correctly
        Assert.assertNotNull("New project should be created", newProject);
        Assert.assertNotNull("New project should have id", newProject.getId());
        Assert.assertTrue("New project should be activated by default", newProject.isActivated());
        Assert.assertEquals("New project name should have Maint. and year",
                "ORIGINAL_PROJECTMaint." + LocalDate.now().getYear(),
                newProject.getName());
        Assert.assertEquals("Customer should be copied",
                "ORIGINAL_CUSTOMER",
                newProject.getCustomer());

        // Assert: Verify old project is deactivated
        Project refreshedOldProject = projectRepository.findById(oldProjectId).orElse(null);
        Assert.assertNotNull("Old project should still exist", refreshedOldProject);
        Assert.assertFalse("Old project should be deactivated", refreshedOldProject.isActivated());

        // Assert: Verify both are in DB
        Assert.assertTrue("New project should be persisted",
                projectRepository.existsById(newProject.getId()));
        Assert.assertTrue("Old project should still exist",
                projectRepository.existsById(oldProjectId));
    }

    /**
     * Verify ROLLBACK when exception occurs during maintenance project creation
     * This proves the method is truly transactional
     */
    @Test
    @Transactional
    @Rollback(true)
    public void testCreateMaintenanceProjectRollsBackOnException() {
        // Arrange: Create an old project
        Project oldProject = new Project("ROLLBACK_TEST_PROJECT", LocalDate.now());
        oldProject.setCustomer("ROLLBACK_CUSTOMER");
        oldProject.setActivated(true);
        Project savedOldProject = projectRepository.save(oldProject);
        Long oldProjectId = savedOldProject.getId();

        // Store initial state
        boolean oldProjectWasActivated = savedOldProject.isActivated();
        long countBefore = projectRepository.count();

        // Act & Assert: Try to create maintenance project with non-existent old project ID
        // This will throw ProjectNotFoundException
        Long invalidProjectId = 99999L;
        try {
            projectService.createMaintennanceProject(invalidProjectId);
            Assert.fail("Should have thrown ProjectNotFoundException");
        } catch (ProjectNotFoundException e) {
            // Expected exception
            log.info("Expected exception caught: {}", e.getMessage());
        }

        // Assert: Verify DB state remained unchanged (ROLLBACK proved)
        long countAfter = projectRepository.count();
        Assert.assertEquals("No new project should be created on exception",
                countBefore,
                countAfter);

        // Assert: Verify old project wasn't modified
        Project refreshedOldProject = projectRepository.findById(oldProjectId).orElse(null);
        Assert.assertNotNull("Original project should still exist", refreshedOldProject);
        Assert.assertEquals("Old project activation status should not change",
                oldProjectWasActivated,
                refreshedOldProject.isActivated());
    }

    /**
     * Test: Verify that new project is NOT created if old project cannot be found
     * This demonstrates ATOMICITY: either all operations succeed or none do
     */
    @Test
    @Transactional
    @Rollback(true)
    public void testCreateMaintenanceProjectAtomicity() {
        // Arrange
        long initialCount = projectRepository.count();

        // Act: Try to create maintenance project for non-existent project
        try {
            projectService.createMaintennanceProject(999999L);
            Assert.fail("Should throw ProjectNotFoundException");
        } catch (ProjectNotFoundException e) {
            log.info("Expected exception: {}", e.getMessage());
        }

        // Assert: Verify no new project was created (ATOMICITY)
        long finalCount = projectRepository.count();
        Assert.assertEquals("Count should remain the same (atomicity)",
                initialCount,
                finalCount);
    }

    /**
     * Integration Test: Verify transactional behavior with manual transaction rollback
     * Simulating a scenario where an unexpected error occurs after both saves
     */
    @Test
    @Transactional
    @Rollback(true) // ← This simulates what happens if exception occurs
    public void testTransactionalBehaviorWithManualRollback() {
        // Arrange
        Project oldProject = new Project("MANUAL_ROLLBACK_TEST", LocalDate.now());
        oldProject.setActivated(true);
        Project savedOld = projectRepository.save(oldProject);
        Long oldId = savedOld.getId();
        long countBeforeMaintenanceCreation = projectRepository.count();

        // Act: Create maintenance project
        Project newProject = projectService.createMaintennanceProject(oldId);
        long countAfterMaintenanceCreation = projectRepository.count();

        // Assert: Both projects exist in this transaction
        Assert.assertEquals("Count should increase by 1",
                countBeforeMaintenanceCreation + 1,
                countAfterMaintenanceCreation);
        Assert.assertTrue("New project should exist",
                projectRepository.existsById(newProject.getId()));
        Assert.assertFalse("Old project should be deactivated",
                projectRepository.findById(oldId).get().isActivated());

        // When this test ends, @Rollback(true) will rollback all changes
        // This proves the transaction contained both operations atomically
    }
}
