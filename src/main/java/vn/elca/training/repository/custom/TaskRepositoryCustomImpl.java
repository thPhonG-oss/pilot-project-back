package vn.elca.training.repository.custom;

import com.querydsl.jpa.impl.JPAQuery;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.QProject;
import vn.elca.training.model.entity.QTask;
import vn.elca.training.model.entity.Task;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * @author gtn
 *
 */
// Rename this class so that Spring can scan and wire this component correctly
public class TaskRepositoryCustomImpl implements TaskRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Project> findProjectsByTaskName(String taskName) {
        return new JPAQuery<Project>(em)
                .from(QProject.project)
                .innerJoin(QProject.project.tasks, QTask.task)
                .fetchJoin()
                .where(QTask.task.name.eq(taskName))
                .fetch();
    }

    @Override
    public List<Task> listRecentTasks(int limit) {
        return new JPAQuery<Task>(em)
                .from(QTask.task)
                .join(QTask.task.project)
                .fetchJoin()
                .orderBy(QTask.task.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Task> listTasksByIds(List<Long> ids){
        return new JPAQuery<Task>(em)
                .from(QTask.task)
                .where(QTask.task.id.in(ids))
                .fetch();
    }
}
