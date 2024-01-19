package elegant.children.catchculture.repository.culturalEvent;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import elegant.children.catchculture.common.constant.Classification;
import elegant.children.catchculture.common.constant.SortType;
import elegant.children.catchculture.common.exception.CustomException;
import elegant.children.catchculture.common.exception.ErrorCode;
import elegant.children.catchculture.dto.culturalEvent.response.CulturalEventDetailsResponseDTO;
import elegant.children.catchculture.dto.culturalEvent.response.CulturalEventListResponseDTO;
import elegant.children.catchculture.entity.culturalevent.Category;
import elegant.children.catchculture.entity.interaction.LikeStar;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.querydsl.core.types.ExpressionUtils.count;
import static elegant.children.catchculture.entity.culturalevent.QCulturalEvent.*;
import static elegant.children.catchculture.entity.interaction.QInteraction.*;
import static elegant.children.catchculture.entity.visitauth.QVisitAuth.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CulturalEventQueryRepository {

    private final JPAQueryFactory queryFactory;

    public boolean existById(final int culturalEventId) {
        return queryFactory.selectOne()
                .from(culturalEvent)
                .where(
                        culturalEventIdEq(culturalEventId)
                )
                .fetchOne() != null;
    }

    public CulturalEventDetailsResponseDTO getCulturalEventDetails(final int culturalEventId, final int userId) {

        if(!existById(culturalEventId)) {
            throw new CustomException(ErrorCode.INVALID_EVENT_ID);
        }

        return queryFactory.select(Projections.fields(CulturalEventDetailsResponseDTO.class,
                        culturalEvent.culturalEventDetail,
                        visitAuth.isAuthenticated.as("isAuthenticated"),
                        culturalEvent.likeCount,
                        ExpressionUtils.as(
                                JPAExpressions.select(count(interaction))
                                        .from(interaction)
                                        .where(
                                                interactionLikeStarEq(),
                                                interaction.culturalEvent.id.eq(culturalEventId)
                                        )
                                , "bookmarkCount")
                        )
                )
                .from(culturalEvent)
                .leftJoin(visitAuth)
                .on(
                        culturalEvent.id.eq(visitAuth.culturalEvent.id),
                        visitAuth.user.id.eq(userId)
                )
                .where(
                        culturalEventIdEq(culturalEventId)
                )
                .fetchOne();
    }


    public List<CulturalEventListResponseDTO> getCulturalEventMainList(final List<Integer> culturalEventIdList) {
        final LocalDateTime now = LocalDateTime.now();

        return queryFactory.select(Projections.constructor(
                        CulturalEventListResponseDTO.class,
                        culturalEvent.id,
                        culturalEvent.culturalEventDetail,
                        culturalEvent.likeCount,
                        culturalEvent.viewCount,
                        Expressions.numberTemplate(Integer.class, "function('datediff', {0}, {1})",
                                culturalEvent.culturalEventDetail.startDate,
                                now).as("remainDay")
                ))
                .from(culturalEvent)
                .where(
                        culturalEvent.id.in(culturalEventIdList)
                )
                .fetch();
    }

    public Page<CulturalEventListResponseDTO> getCulturalEventList(final List<Category> categoryList, final Pageable pageable, final SortType sortType) {

        final LocalDateTime now = LocalDateTime.now();

        final List<CulturalEventListResponseDTO> content = queryFactory.select(Projections.constructor(
                        CulturalEventListResponseDTO.class,
                        culturalEvent.id,
                        culturalEvent.culturalEventDetail,
                        culturalEvent.likeCount,
                        culturalEvent.viewCount,
                        Expressions.numberTemplate(Integer.class, "function('datediff', {0}, {1})",
                                culturalEvent.culturalEventDetail.startDate,
                                now).as("remainDay")
                ))
                .from(culturalEvent)
                .where(
                        notFinishedCulturalEvent(now),
                        categoryIn(categoryList)

                )
                .orderBy(
                        setOrderWithSortType(sortType)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final long count = queryFactory
                .select(culturalEvent.count())
                .from(culturalEvent)
                .where(
                        notFinishedCulturalEvent(now),
                        categoryIn(categoryList)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, count);
    }



    public Page<CulturalEventListResponseDTO> getCulturalEventListWithCondition(final String keyword, final List<Category> categoryList,
                                                                                final Pageable pageable, final SortType sortType) {

        final LocalDateTime now = LocalDateTime.now();

        final List<CulturalEventListResponseDTO> content = queryFactory.select(Projections.constructor(
                        CulturalEventListResponseDTO.class,
                        culturalEvent.id,
                        culturalEvent.culturalEventDetail,
                        culturalEvent.likeCount,
                        culturalEvent.viewCount,
                        Expressions.numberTemplate(Integer.class, "function('datediff', {0}, {1})",
                                culturalEvent.culturalEventDetail.startDate,
                                now).as("remainDay")
                ))
                .from(culturalEvent)
                .where(
                        notFinishedCulturalEvent(now),
                        titleContains(keyword),
                        categoryIn(categoryList)
                ).orderBy(
                        setOrderWithSortType(sortType)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        final long count = queryFactory
                .select(culturalEvent.count())
                .from(culturalEvent)
                .where(
                        notFinishedCulturalEvent(now),
                        titleContains(keyword),
                        categoryIn(categoryList)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, count);

    }

    public Page<CulturalEventListResponseDTO> getCulturalEventResponseDTOWithUser(final List<Category> categoryList, final int userId,
                                                                                  final Pageable pageable, final Classification classification) {

        final LocalDateTime now = LocalDateTime.now();
        List<CulturalEventListResponseDTO> content = null;
        long count = 0;

        switch (classification) {
            case LIKE, STAR -> {
                final LikeStar likeStar = LikeStar.of(classification);
                content = getLikeStarContent(categoryList, userId, classification, now, likeStar);
                count = getLikeStarCount(categoryList, userId, now, likeStar);
            }
            case VISIT_AUTH -> {
                content = getAuthenticatedContent(categoryList, userId, classification, now);
                count = getAuthenticatedCount(categoryList, userId, now);
            }
        }
          return new PageImpl<>(content, pageable, count);
    }

    private static BooleanExpression interactionLikeStarEq() {
        return interaction.likeStar.eq(LikeStar.STAR);
    }

    private BooleanExpression culturalEventIdEq(final int culturalEventId) {
        return culturalEvent.id.eq(culturalEventId);
    }

    private BooleanExpression titleContains(final String keyword) {
        return keyword == null ? null : culturalEvent.culturalEventDetail.title.contains(keyword);
    }

    public BooleanExpression userIdEqWithVisitAuth(final int userId) {
        return userId == 0 ? null : visitAuth.user.id.eq(userId);
    }

    public BooleanExpression userIdEqWithInteraction(final int userId) {
        return userId == 0 ? null : interaction.user.id.eq(userId);
    }


    private BooleanExpression categoryIn(final List<Category> categoryList) {
        return categoryList == null || categoryList.isEmpty() ? null : culturalEvent.culturalEventDetail.category.in(categoryList);
    }

    private BooleanExpression notFinishedCulturalEvent(final LocalDateTime now) {
        return culturalEvent.culturalEventDetail.endDate.goe(now);
    }

    private static void startDateASC(final List<OrderSpecifier> orderSpecifier) {
        orderSpecifier.add(new OrderSpecifier<>(Order.ASC, culturalEvent.culturalEventDetail.startDate));
    }


    private OrderSpecifier[] setOrderWithSortType(final SortType sortType) {

        List<OrderSpecifier> orderSpecifier = new ArrayList<>();
        switch (sortType) {
            case VIEW_COUNT -> {
                orderSpecifier.add(new OrderSpecifier<>(Order.DESC, culturalEvent.viewCount));
                startDateASC(orderSpecifier);
            }
            case LIKE -> {
                orderSpecifier.add(new OrderSpecifier<>(Order.DESC, culturalEvent.likeCount));
                startDateASC(orderSpecifier);
            }
            case RECENT -> {
                startDateASC(orderSpecifier);
                orderSpecifier.add(new OrderSpecifier<>(Order.ASC, culturalEvent.culturalEventDetail.endDate));
            }

        }
        return orderSpecifier.toArray(new OrderSpecifier[0]);
    }

    private OrderSpecifier[] setOrderWithClassification(final Classification classification) {

        final List<OrderSpecifier> orderSpecifier = new ArrayList<>();
        switch (classification) {
            case VISIT_AUTH -> orderSpecifier.add(new OrderSpecifier<>(Order.DESC, visitAuth.createdAt));
            case LIKE , STAR -> orderSpecifier.add(new OrderSpecifier<>(Order.DESC, interaction.createdAt));
        }
        startDateASC(orderSpecifier);

        return orderSpecifier.toArray(new OrderSpecifier[0]);
    }

    private Long getAuthenticatedCount(List<Category> categoryList, int userId, LocalDateTime now) {
        return queryFactory
                .select(culturalEvent.count())
                .from(culturalEvent)
                .innerJoin(visitAuth)
                .on(
                        culturalEvent.id.eq(visitAuth.culturalEvent.id),
                        visitAuth.user.id.eq(userId)
                )
                .where(
                        notFinishedCulturalEvent(now),
                        categoryIn(categoryList),
                        userIdEqWithVisitAuth(userId)
                ).fetchOne();
    }

    private List<CulturalEventListResponseDTO> getAuthenticatedContent(List<Category> categoryList, int userId, Classification classification, LocalDateTime now) {
        return queryFactory.select(
                        Projections.constructor(
                                CulturalEventListResponseDTO.class,
                                culturalEvent.id,
                                culturalEvent.culturalEventDetail,
                                culturalEvent.likeCount,
                                culturalEvent.viewCount,
                                Expressions.numberTemplate(Integer.class, "function('datediff', {0}, {1})",
                                        culturalEvent.culturalEventDetail.startDate,
                                        now).as("remainDay"),
                                visitAuth.isAuthenticated
                        )
                )
                .from(culturalEvent)
                .innerJoin(visitAuth)
                .on(
                        culturalEvent.id.eq(visitAuth.culturalEvent.id),
                        visitAuth.user.id.eq(userId)
                )
                .where(
                        notFinishedCulturalEvent(now),
                        categoryIn(categoryList),
                        userIdEqWithVisitAuth(userId)
                ).orderBy(
                        setOrderWithClassification(classification)
                ).fetch();
    }



    private Long getLikeStarCount(List<Category> categoryList, int userId, LocalDateTime now, LikeStar likeStar) {
        return queryFactory
                .select(culturalEvent.count())
                .from(culturalEvent)
                .innerJoin(interaction)
                .on(
                        culturalEvent.id.eq(interaction.culturalEvent.id),
                        interaction.user.id.eq(userId),
                        interaction.likeStar.eq(likeStar)
                )
                .where(
                        notFinishedCulturalEvent(now),
                        categoryIn(categoryList),
                        userIdEqWithInteraction(userId)
                ).fetchOne();
    }

    private List<CulturalEventListResponseDTO> getLikeStarContent(List<Category> categoryList, int userId, Classification classification, LocalDateTime now, LikeStar likeStar) {
        return queryFactory.select(
                        Projections.constructor(
                                CulturalEventListResponseDTO.class,
                                culturalEvent.id,
                                culturalEvent.culturalEventDetail,
                                culturalEvent.likeCount,
                                culturalEvent.viewCount,
                                Expressions.numberTemplate(Integer.class, "function('datediff', {0}, {1})",
                                        culturalEvent.culturalEventDetail.startDate,
                                        now).as("remainDay")
                        )
                )
                .from(culturalEvent)
                .innerJoin(interaction)
                .on(
                        culturalEvent.id.eq(interaction.culturalEvent.id),
                        interaction.user.id.eq(userId),
                        interaction.likeStar.eq(likeStar)
                )
                .where(
                        notFinishedCulturalEvent(now),
                        categoryIn(categoryList),
                        userIdEqWithInteraction(userId)
                ).orderBy(
                        setOrderWithClassification(classification)
                ).fetch();
    }
}
