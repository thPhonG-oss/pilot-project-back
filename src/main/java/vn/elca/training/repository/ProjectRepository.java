package vn.elca.training.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.elca.training.model.entity.Project;
import vn.elca.training.repository.custom.ProjectRepositoryCustom;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author vlp
 *
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, QuerydslPredicateExecutor<Project>, ProjectRepositoryCustom {
    Page<Project> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByProjectNumber(Long projectNumber);

    @EntityGraph(attributePaths = {"employees", "group", "group.leader"})
    Optional<Project> findProjectById(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM project_employee WHERE project_id IN :ids", nativeQuery = true)
    void deleteEmployeeLinksByProjectIds(@Param("ids") Collection<Long> ids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Project p WHERE p.id IN :ids")
    void deleteProjectsByIds(@Param("ids") Collection<Long> ids);
}
