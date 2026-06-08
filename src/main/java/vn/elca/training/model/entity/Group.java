package vn.elca.training.model.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "groups")
public class Group extends BaseEntity{
    @OneToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinColumn(name = "group_leader_id")
    private Employee leader;

    @OneToMany(
            mappedBy = "group",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private List<Project> projects = new ArrayList<>();

    public Group() {}

    public Group(Employee leader) {
        this.leader = leader;
    }

    public Employee getLeader() {
        return leader;
    }

    public void setLeader(Employee leader) {
        this.leader = leader;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
}
