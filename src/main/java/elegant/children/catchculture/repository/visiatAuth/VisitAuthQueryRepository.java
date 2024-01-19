package elegant.children.catchculture.repository.visiatAuth;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import elegant.children.catchculture.dto.admin.response.VisitAuthResponseDTO;
import elegant.children.catchculture.dto.admin.response.VisitAuthResponseListDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static elegant.children.catchculture.common.constant.PageSize.*;
import static elegant.children.catchculture.entity.culturalevent.QCulturalEvent.*;
import static elegant.children.catchculture.entity.user.QUser.*;
import static elegant.children.catchculture.entity.visitauth.QVisitAuth.*;

@Repository
@Slf4j
@RequiredArgsConstructor
public class VisitAuthQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<VisitAuthResponseDTO> findById(final int visitAuthId) {
        VisitAuthResponseDTO visitAuthResponseDTO = queryFactory.select(Projections.fields(VisitAuthResponseDTO.class,
                        visitAuth.id,
                        user.id.as("userId"),
                        user.nickname,
                        culturalEvent.id.as("culturalEventId"),
                        culturalEvent.culturalEventDetail.title,
                        culturalEvent.culturalEventDetail.description,
                        visitAuth.storedFileUrl
                ))
                .from(visitAuth)
                .innerJoin(culturalEvent)
                .on(visitAuth.culturalEvent.id.eq(culturalEvent.id))
                .innerJoin(user)
                .on(visitAuth.user.id.eq(user.id))
                .where(
                        visitAuth.isAuthenticated.eq(false),
                        visitAuth.id.eq(visitAuthId)
                )
                .fetchOne();

        return Optional.ofNullable(visitAuthResponseDTO);
    }

    public Slice<VisitAuthResponseListDTO> getNotAuthenticatedVisitAuthList(final int lastId) {
        List<VisitAuthResponseListDTO> content = queryFactory.select(Projections.fields(VisitAuthResponseListDTO.class,
                        visitAuth.id,
                        user.nickname,
                        culturalEvent.culturalEventDetail.title,
                        visitAuth.createdAt
                ))
                .from(visitAuth)
                .innerJoin(culturalEvent)
                .on(visitAuth.culturalEvent.id.eq(culturalEvent.id))
                .innerJoin(user)
                .on(visitAuth.user.id.eq(user.id))
                .where(
                        visitAuth.isAuthenticated.eq(false),
                        visitAuthIdGt(lastId)
                )
                .orderBy(visitAuth.id.asc())
                .limit(OTHER_PAGE_SIZE + 1l)
                .fetch();

        boolean hasNext = false;

        if(content.size() == OTHER_PAGE_SIZE + 1l) {
            content.remove(OTHER_PAGE_SIZE);
            hasNext = true;
        }
        return new SliceImpl<>(content, PageRequest.ofSize(OTHER_PAGE_SIZE), hasNext);

    }

    private static BooleanExpression visitAuthIdGt(int lastId) {
        return lastId == 0 ? null : visitAuth.id.gt(lastId);
    }
}
