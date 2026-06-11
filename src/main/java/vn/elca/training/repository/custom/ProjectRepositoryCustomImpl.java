package vn.elca.training.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryFactory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.QProject;
import vn.elca.training.model.entity.Status;
import vn.elca.training.util.ApplicationUtils;
import vn.elca.training.util.PaginationUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class ProjectRepositoryCustomImpl implements ProjectRepositoryCustom{
    private final EntityManager entityManager;

    public ProjectRepositoryCustomImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Page<Project> findProjectsByCriteria(String keyword, Status status, Pageable pageable) {
        QProject project = QProject.project;

        BooleanBuilder builder = buildCondition(keyword, status);

        List<Project> projects = new JPAQuery<Project>(entityManager)
                .select(project)
                .from(project)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(project.projectNumber.asc())
                .fetch();

        Long total = new JPAQuery<Long>(entityManager)
                .select(project.count())
                .from(project)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(projects, pageable, total == null ? 0 : total);
    }

    private BooleanBuilder buildCondition(String keyword, Status status){
        QProject project = QProject.project;

        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String value = keyword.trim();

            BooleanExpression condition = project.name.containsIgnoreCase(value)
                    .or(project.customer.containsIgnoreCase(value));

            if (ApplicationUtils.isLong(value)) {
                condition = condition.or(project.projectNumber.eq(Long.valueOf(value)));
            }

            builder.and(condition);
        }

        if (status != null) {
            builder.and(project.status.eq(status));
        }


        return builder;
    }
}
