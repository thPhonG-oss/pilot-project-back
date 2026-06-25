package vn.elca.training.testutil;

import vn.elca.training.model.entity.Project;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

public final class TestEntityFactory {

    private static final AtomicLong PROJECT_NUMBER_SEQUENCE = new AtomicLong(90_000L);

    private TestEntityFactory() {
    }

    public static Project project(String name, LocalDate startDate) {
        return project(name, startDate, name + "_CUSTOMER");
    }

    public static Project project(String name, LocalDate startDate, String customer) {
        Project project = new Project(name, startDate);
        project.setProjectNumber(PROJECT_NUMBER_SEQUENCE.incrementAndGet());
        project.setCustomer(customer);
        return project;
    }
}
