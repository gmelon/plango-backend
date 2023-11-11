package dev.gmelon.plango.domain.schedule.query.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * dev.gmelon.plango.domain.schedule.query.dto.QScheduleCountQueryDto is a Querydsl Projection type for ScheduleCountQueryDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QScheduleCountQueryDto extends ConstructorExpression<ScheduleCountQueryDto> {

    private static final long serialVersionUID = -2021501415L;

    public QScheduleCountQueryDto(com.querydsl.core.types.Expression<java.time.LocalDate> date, com.querydsl.core.types.Expression<Integer> doneCount, com.querydsl.core.types.Expression<Integer> totalCount) {
        super(ScheduleCountQueryDto.class, new Class<?>[]{java.time.LocalDate.class, int.class, int.class}, date, doneCount, totalCount);
    }

}

