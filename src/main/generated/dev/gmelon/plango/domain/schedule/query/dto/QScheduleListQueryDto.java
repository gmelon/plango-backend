package dev.gmelon.plango.domain.schedule.query.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * dev.gmelon.plango.domain.schedule.query.dto.QScheduleListQueryDto is a Querydsl Projection type for ScheduleListQueryDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QScheduleListQueryDto extends ConstructorExpression<ScheduleListQueryDto> {

    private static final long serialVersionUID = -1605047198L;

    public QScheduleListQueryDto(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<String> content, com.querydsl.core.types.Expression<java.time.LocalDate> date, com.querydsl.core.types.Expression<java.time.LocalTime> startTime, com.querydsl.core.types.Expression<java.time.LocalTime> endTime, com.querydsl.core.types.Expression<Integer> memberCount, com.querydsl.core.types.Expression<Boolean> isOwner, com.querydsl.core.types.Expression<Boolean> isAccepted, com.querydsl.core.types.Expression<Boolean> done) {
        super(ScheduleListQueryDto.class, new Class<?>[]{long.class, String.class, String.class, java.time.LocalDate.class, java.time.LocalTime.class, java.time.LocalTime.class, int.class, boolean.class, boolean.class, boolean.class}, id, title, content, date, startTime, endTime, memberCount, isOwner, isAccepted, done);
    }

}

