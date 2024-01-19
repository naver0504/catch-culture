package elegant.children.catchculture.repository.eventReport;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import elegant.children.catchculture.dto.admin.response.EventReportResponseDTO;
import elegant.children.catchculture.dto.admin.response.EventReportResponseListDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static elegant.children.catchculture.common.constant.PageSize.*;
import static elegant.children.catchculture.entity.eventreport.QEventReport.*;
import static elegant.children.catchculture.entity.user.QUser.*;

@Repository
@RequiredArgsConstructor
public class EventReportQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Slice<EventReportResponseListDTO> getEventReportList(final int lastId) {

        final List<EventReportResponseListDTO> content = queryFactory.select(Projections.fields(EventReportResponseListDTO.class,
                        eventReport.id,
                        eventReport.user.nickname,
                        eventReport.culturalEventDetail.title,
                        eventReport.createdAt
                ))
                .from(eventReport)
                .innerJoin(user)
                .on(eventReport.user.id.eq(user.id))
                .where(
                        eventReportIdGt(lastId),
                        eventReport.isReported.eq(false)
                ).limit(OTHER_PAGE_SIZE + 1L)
                .orderBy(eventReport.id.asc())
                .fetch();

        boolean hasNext = false;
        if (content.size() == OTHER_PAGE_SIZE + 1L) {
            hasNext = true;
            content.remove(OTHER_PAGE_SIZE);
        }

        return new SliceImpl<>(content, PageRequest.ofSize(OTHER_PAGE_SIZE), hasNext);

    }

    private static BooleanExpression eventReportIdGt(int lastId) {
        return lastId == 0 ? null : eventReport.id.gt(lastId);
    }

    public Optional<EventReportResponseDTO> getEventReport(final int eventReportId) {

        EventReportResponseDTO eventReportResponseDTO = queryFactory.select(Projections.fields(EventReportResponseDTO.class,
                        eventReport.id,
                        eventReport.user.id.as("userId"),
                        user.nickname,
                        eventReport.culturalEventDetail
                ))
                .from(eventReport)
                .innerJoin(user)
                .on(eventReport.user.id.eq(user.id))
                .where(
                        eventReport.id.eq(eventReportId)
                )
                .fetchOne();

        return Optional.ofNullable(eventReportResponseDTO);

    }
}
