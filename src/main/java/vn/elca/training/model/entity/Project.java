package vn.elca.training.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Project extends BaseEntity{
    @Column(unique = true, nullable = false)
    private Long projectNumber;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String customer;

    @Column
    @Enumerated(value = EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

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

    public Project(String name, LocalDate startDate) {
        this.name = name;
        this.startDate = startDate;
    }
}