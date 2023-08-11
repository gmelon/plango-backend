package dev.gmelon.plango.domain.place;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static dev.gmelon.plango.domain.place.QPlaceSearchRecord.placeSearchRecord;
import static java.lang.Math.max;

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
