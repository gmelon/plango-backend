package dev.gmelon.plango.domain.schedule.repository;

import static dev.gmelon.plango.domain.schedule.entity.QSchedule.schedule;
import static dev.gmelon.plango.domain.schedule.entity.QScheduleMember.scheduleMember;
import static java.lang.Math.max;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.gmelon.plango.domain.schedule.entity.Schedule;
import java.util.List;
import lombok.RequiredArgsConstructor;

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
                .orderBy(schedule.date.desc())
                .orderBy(schedule.startTime.desc().nullsFirst())
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
