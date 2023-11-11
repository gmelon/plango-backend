package dev.gmelon.plango.domain.schedule.query;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * dev.gmelon.plango.domain.schedule.query.QScheduleQueryRepositoryImpl_ConfirmedSchedulePlaceQueryDto is a Querydsl Projection type for ConfirmedSchedulePlaceQueryDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QScheduleQueryRepositoryImpl_ConfirmedSchedulePlaceQueryDto extends ConstructorExpression<ScheduleQueryRepositoryImpl.ConfirmedSchedulePlaceQueryDto> {

    private static final long serialVersionUID = 800136898L;

    public QScheduleQueryRepositoryImpl_ConfirmedSchedulePlaceQueryDto(com.querydsl.core.types.Expression<Long> scheduleId, com.querydsl.core.types.Expression<String> placeName) {
        super(ScheduleQueryRepositoryImpl.ConfirmedSchedulePlaceQueryDto.class, new Class<?>[]{long.class, String.class}, scheduleId, placeName);
    }

}

