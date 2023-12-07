package dev.gmelon.plango.domain.diary;

import static dev.gmelon.plango.domain.diary.QDiary.diary;
import static dev.gmelon.plango.domain.schedule.QSchedule.schedule;
import static java.lang.Math.max;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DiaryRepositoryImpl implements DiaryRepositoryCustom {

    private static final int DEFAULT_PAGINATION_SIZE = 10;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Diary> search(Long memberId, String trimmedQuery, int page) {
        return jpaQueryFactory
                .selectFrom(diary)
                .join(diary.schedule, schedule).fetchJoin()
                .where(trim(diary.content).contains(trimmedQuery)
                        .and(diary.member.id.eq(memberId)))
                .orderBy(schedule.date.desc())
                .orderBy(schedule.startTime.desc().nullsFirst())
                .offset(offset(page, DEFAULT_PAGINATION_SIZE))
                .limit(DEFAULT_PAGINATION_SIZE)
                .fetch();
    }

    private StringTemplate trim(StringPath field) {
        return Expressions.stringTemplate("function('replace', {0}, {1}, {2})", field, " ", "");
    }

    @Override
    public List<Diary> findAllByMemberId(Long memberId, int page, int size) {
        return jpaQueryFactory
                .selectFrom(diary)
                .where(diary.member.id.eq(memberId))
                .join(diary.schedule, schedule)
                .orderBy(schedule.date.desc())
                .orderBy(schedule.startTime.desc().nullsFirst())
                .offset(offset(page, size))
                .limit(size)
                .fetch();
    }

    private int offset(int page, int size) {
        return (max(1, page) - 1) * size;
    }

}
