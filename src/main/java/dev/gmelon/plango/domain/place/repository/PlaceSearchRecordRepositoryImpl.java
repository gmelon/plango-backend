package dev.gmelon.plango.domain.place.repository;

import static dev.gmelon.plango.domain.place.entity.QPlaceSearchRecord.placeSearchRecord;
import static java.lang.Math.max;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.gmelon.plango.domain.place.entity.PlaceSearchRecord;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlaceSearchRecordRepositoryImpl implements PlaceSearchRecordRepositoryCustom {

    private static final int DEFAULT_PAGINATION_SIZE = 40;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<PlaceSearchRecord> findAllByMemberId(Long memberId, int page) {
        return jpaQueryFactory.selectFrom(placeSearchRecord)
                .where(placeSearchRecord.member.id.eq(memberId))
                .limit(DEFAULT_PAGINATION_SIZE)
                .offset(offset(page))
                .orderBy(placeSearchRecord.lastSearchedDate.desc())
                .fetch();
    }

    private int offset(int page) {
        return (max(1, page) - 1) * DEFAULT_PAGINATION_SIZE;
    }
}
