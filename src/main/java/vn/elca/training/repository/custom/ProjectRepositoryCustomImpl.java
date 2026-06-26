package vn.elca.training.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.QProject;
import vn.elca.training.model.entity.Status;
import vn.elca.training.util.LikeEscapeUtil;

import javax.persistence.EntityManager;
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

        Long total = new JPAQuery<Long>(entityManager)
                .select(project.count())
                .from(project)
                .where(builder)
                .fetchOne();

        // early return if no data
        if(total == null || total == 0L) {
            return Page.empty(pageable);
        }

        // guard against out-of-range page requests
        if (pageable.getOffset() >= total) {
            return Page.empty(pageable);
        }

        List<Project> projects = new JPAQuery<Project>(entityManager)
                .select(project)
                .from(project)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(project.projectNumber.asc())
                .fetch();

        return new PageImpl<>(projects, pageable, total);
    }

    private BooleanBuilder buildCondition(String keyword, Status status){
        QProject project = QProject.project;

        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String value = LikeEscapeUtil.escape(keyword.trim());
            char escape = LikeEscapeUtil.ESCAPE_CHAR;

            BooleanExpression condition = project.name.lower().like("%" + value.toLowerCase() + "%", escape)
                    .or(project.customer.lower().like("%" + value.toLowerCase() + "%", escape));
            try {
                Long.parseLong(keyword.trim());
                condition = condition.or(project.projectNumber.stringValue().like("%" + value + "%", escape));
            }
            catch (NumberFormatException ignored) {
            }

            builder.and(condition);
        }

        if (status != null) {
            builder.and(project.status.eq(status));
        }

        return builder;
    }
}
