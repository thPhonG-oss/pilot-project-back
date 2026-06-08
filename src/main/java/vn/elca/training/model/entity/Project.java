package vn.elca.training.model.entity;

import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author vlp
 */
@Entity
public class Project extends BaseEntity{
    @Column(nullable = false)
    private String name;

    @Column
    private LocalDate finishingDate;

    @Column(nullable = true, length = 50)
    private String customer;

    @Column
    @Enumerated(value = EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private Set<Task> tasks = new HashSet<>();

    @ManyToMany(
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinTable(
            name = "project_employee",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private List<Employee> employees = new ArrayList<>();

    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "group_id")
    private Group group;

    public Project() {
    }

    public Project(String name, LocalDate finishingDate) {
        this.name = name;
        this.finishingDate = finishingDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getFinishingDate() {
        return finishingDate;
    }

    public void setFinishingDate(LocalDate finishingDate) {
        this.finishingDate = finishingDate;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    // helper methods
}