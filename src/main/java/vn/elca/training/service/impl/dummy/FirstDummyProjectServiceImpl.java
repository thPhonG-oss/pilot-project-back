package vn.elca.training.service.impl.dummy;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import vn.elca.training.model.dto.ProjectDto;
import vn.elca.training.model.entity.Project;
import vn.elca.training.service.ProjectService;

import java.util.List;
import java.util.Optional;

/**
 * @author gtn
 *
 */
@Component
@Profile("dummy")
@Primary
public class FirstDummyProjectServiceImpl extends AbstractDummyProjectService implements ProjectService {

    @Override
    public List<Project> findAll() {
        throw new UnsupportedOperationException("This is first dummy service");
    }

    @Override
    public long count() {
        printCurrentActiveProfiles();
        throw new UnsupportedOperationException("This is first dummy service");
    }

    @Override
    public Page<Project> findAllProjectsContainingIgnoreCase(String keyword) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Project findProjectById(Long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Project updateProject(Long id, ProjectDto projectDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Project createMaintennanceProject(Long oldProjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
