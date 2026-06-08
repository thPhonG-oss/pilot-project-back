package vn.elca.training.model.entity;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    @ColumnDefault("0")
    private Long version;

    @Column(nullable = false)
    @ColumnDefault("true")
    private boolean activated;

    public BaseEntity() {}

    protected BaseEntity(Long id) {
        this.id = id;
    }

    protected BaseEntity(Long id, Long version) {
        this.id = id;
        this.version = version;
    }

    public BaseEntity(Long id, Long version, boolean activated) {
        this.id = id;
        this.version = version;
        this.activated = activated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    @PrePersist
    public void prePersist(){
        this.activated = true;
    }
}
