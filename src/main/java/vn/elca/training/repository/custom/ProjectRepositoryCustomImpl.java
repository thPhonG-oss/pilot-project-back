package vn.elca.training.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import vn.elca.training.model.dto.request.ProjectSearchCondition;
import vn.elca.training.model.entity.*;
import vn.elca.training.util.LikeEscapeUtil;

import javax.persistence.EntityManager;
import java.util.List;

@Slf4j
public class ProjectRepositoryCustomImpl implements ProjectRepositoryCustom{
    private final EntityManager entityManager;
    private final QProject project = QProject.project;

    public ProjectRepositoryCustomImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Page<Project> findProjectsByCriteria(ProjectSearchCondition condition, Pageable pageable) {
        log.info("Start searching: ");
        BooleanBuilder builder = buildCondition(condition);

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

        OrderSpecifier<?>[] orderSpecifiers = buildOrderSpecifiers(pageable.getSort());

        List<Project> projects = new JPAQuery<Project>(entityManager)
                .select(project)
                .from(project)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers)
                .fetch();

        return new PageImpl<>(projects, pageable, total);
    }

    private BooleanBuilder buildCondition(ProjectSearchCondition condition){
        BooleanBuilder builder = new BooleanBuilder();

        addKeywordCondition(builder, condition.getKeyword());
        addStatusCondition(builder, condition.getStatus());
        addDateConditions(builder, condition);
        addLeaderCondition(builder, condition.getLeaderVisa());
        addMemberCondition(builder, condition.getMemberVisa());

        return builder;
    }

     private void addKeywordCondition(BooleanBuilder builder, String keyword){
        if (!hasText(keyword)) {
            return;
        }

        keyword = keyword.trim();

        String escaped = LikeEscapeUtil.escape(keyword);
        String lower = escaped.toLowerCase();
        char escape = LikeEscapeUtil.ESCAPE_CHAR;

        BooleanExpression predicate =
                project.name.lower().like("%" + lower + "%", escape)
                        .or(project.customer.lower().like("%" + lower + "%", escape));

        if (keyword.chars().allMatch(Character::isDigit)) {
            predicate = predicate.or(
                    project.projectNumber.stringValue()
                            .like("%" + escaped + "%", escape)
            );
        }

        builder.and(predicate);
    }

    private void addStatusCondition(BooleanBuilder builder, Status status){
        if(status != null){
            builder.and(project.status.eq(status));
        }
    }

    private void addDateConditions(BooleanBuilder builder,
                                   ProjectSearchCondition condition) {

        if (condition.getStartDateFrom() != null) {
            builder.and(project.startDate.goe(condition.getStartDateFrom()));
        }

        if (condition.getStartDateTo() != null) {
            builder.and(project.startDate.loe(condition.getStartDateTo()));
        }

        if (condition.getEndDateFrom() != null) {
            builder.and(project.endDate.goe(condition.getEndDateFrom()));
        }

        if (condition.getEndDateTo() != null) {
            builder.and(project.endDate.loe(condition.getEndDateTo()));
        }
    }

    private void addLeaderCondition(BooleanBuilder builder, String visa) {

        if (hasText(visa)) {
            builder.and(
                    project.group.leader.visa.equalsIgnoreCase(visa.trim())
            );
        }
    }

    private void addMemberCondition(BooleanBuilder builder, String visa) {

        if (hasText(visa)) {
            builder.and(memberExists(visa.trim()));
        }
    }

    private BooleanExpression memberExists(String visa) {
        QEmployee memberAlias = new QEmployee("memberAlias");

        // "project MEMBER OF memberAlias.projects" avoids joining the many-to-many table,
        // so the outer query never fans out regardless of how many members a project has
        return JPAExpressions
                .selectOne()
                .from(memberAlias)
                .where(memberAlias.visa.equalsIgnoreCase(visa), memberAlias.projects.contains(project))
                .exists();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private OrderSpecifier<?>[] buildOrderSpecifiers(Sort sort) {
        if (sort.isUnsorted()) {
            return new OrderSpecifier[]{ project.projectNumber.desc() }; // default fallback
        }

        return sort.stream()
                .map(order -> {
                    PathBuilder<Project> pathBuilder = new PathBuilder<>(Project.class, "project");

                    Order direction = order.isAscending() ? Order.ASC : Order.DESC;

                    return new OrderSpecifier(direction, pathBuilder.get(order.getProperty()));
                })
                .toArray(OrderSpecifier[]::new);
    }
}
