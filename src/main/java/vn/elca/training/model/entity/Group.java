package vn.elca.training.model.entity;

import javax.persistence.*;

@Entity
public class Group extends BaseEntity{
    @OneToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinColumn(name = "group_leader_id")
    private Employee leader;

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
}
