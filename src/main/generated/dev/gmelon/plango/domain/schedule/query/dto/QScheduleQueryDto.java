package dev.gmelon.plango.domain.schedule.query.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * dev.gmelon.plango.domain.schedule.query.dto.QScheduleQueryDto is a Querydsl Projection type for ScheduleQueryDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QScheduleQueryDto extends ConstructorExpression<ScheduleQueryDto> {

    private static final long serialVersionUID = -1878313948L;

    public QScheduleQueryDto(com.querydsl.core.types.Expression<Long> scheduleId, com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<String> content, com.querydsl.core.types.Expression<java.time.LocalDate> date, com.querydsl.core.types.Expression<java.time.LocalTime> startTime, com.querydsl.core.types.Expression<java.time.LocalTime> endTime, com.querydsl.core.types.Expression<Boolean> isDone, com.querydsl.core.types.Expression<Boolean> hasDiary) {
        super(ScheduleQueryDto.class, new Class<?>[]{long.class, String.class, String.class, java.time.LocalDate.class, java.time.LocalTime.class, java.time.LocalTime.class, boolean.class, boolean.class}, scheduleId, title, content, date, startTime, endTime, isDone, hasDiary);
    }

}

