package dev.gmelon.plango.domain.schedule.place;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static dev.gmelon.plango.domain.schedule.QSchedule.schedule;
import static dev.gmelon.plango.domain.schedule.place.QSchedulePlace.schedulePlace;
import static java.lang.Math.max;

@RequiredArgsConstructor
public class SchedulePlaceRepositoryImpl implements SchedulePlaceRepositoryCustom {

    private static final int DEFAULT_PAGINATION_SIZE = 10;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<SchedulePlace> search(Long memberId, String trimmedQuery, int page) {
        return jpaQueryFactory
                .selectFrom(schedulePlace)
                .join(schedulePlace.schedule, schedule).fetchJoin()
                .where(trim(schedulePlace.placeName).contains(trimmedQuery))
                .offset(offset(page))
                .limit(DEFAULT_PAGINATION_SIZE)
                .distinct()
                .fetch();
    }

    private StringTemplate trim(StringPath field) {
        return Expressions.stringTemplate("function('replace', {0}, {1}, {2})", field, " ", "");
    }

    private int offset(int page) {
        return (max(1, page) - 1) * DEFAULT_PAGINATION_SIZE;
    }

}
