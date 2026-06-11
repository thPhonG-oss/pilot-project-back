package vn.elca.training.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.Status;

public interface ProjectRepositoryCustom {
    Page<Project> findProjectsByCriteria(String keyword, Status status, Pageable pageable);
}
