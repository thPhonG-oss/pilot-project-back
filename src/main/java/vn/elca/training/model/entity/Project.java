package vn.elca.training.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vlp
 */
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Project extends BaseEntity{
    @Column(unique = true, nullable = false)
    private Long projectNumber;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String customer;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Status status=Status.NEW;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @ManyToMany(
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY
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

    public Project(String name, LocalDate startDate) {
        this.name = name;
        this.startDate = startDate;
    }
}