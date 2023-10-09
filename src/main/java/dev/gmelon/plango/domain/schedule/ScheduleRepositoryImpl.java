package dev.gmelon.plango.domain.schedule;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static dev.gmelon.plango.domain.schedule.QSchedule.schedule;
import static dev.gmelon.plango.domain.schedule.QScheduleMember.scheduleMember;
import static java.lang.Math.max;

@RequiredArgsConstructor
public class ScheduleRepositoryImpl implements ScheduleRepositoryCustom {

    private static final int DEFAULT_PAGINATION_SIZE = 10;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Schedule> search(Long memberId, String trimmedQuery, int page) {
        return jpaQueryFactory
                .selectFrom(schedule)
                .join(schedule.scheduleMembers, scheduleMember)
                .on(scheduleMember.member.id.eq(memberId))
                .where(trim(schedule.title).contains(trimmedQuery)
                        .or(trim(schedule.content).contains(trimmedQuery)))
                .offset(offset(page))
                .limit(DEFAULT_PAGINATION_SIZE)
                .fetch();
    }

    private StringTemplate trim(StringPath field) {
        return Expressions.stringTemplate("function('replace', {0}, {1}, {2})", field, " ", "");
    }

    private int offset(int page) {
        return (max(1, page) - 1) * DEFAULT_PAGINATION_SIZE;
    }

}