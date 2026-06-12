package vn.elca.training.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.elca.training.model.entity.Group;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    @Override
    @EntityGraph(attributePaths = "leader")
    List<Group> findAll(Sort sort);
}
