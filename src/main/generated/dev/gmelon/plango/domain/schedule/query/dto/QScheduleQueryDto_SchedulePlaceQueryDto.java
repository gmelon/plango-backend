package dev.gmelon.plango.domain.schedule.query.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * dev.gmelon.plango.domain.schedule.query.dto.QScheduleQueryDto_SchedulePlaceQueryDto is a Querydsl Projection type for SchedulePlaceQueryDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QScheduleQueryDto_SchedulePlaceQueryDto extends ConstructorExpression<ScheduleQueryDto.SchedulePlaceQueryDto> {

    private static final long serialVersionUID = 523864669L;

    public QScheduleQueryDto_SchedulePlaceQueryDto(com.querydsl.core.types.Expression<Long> placeId, com.querydsl.core.types.Expression<String> placeName, com.querydsl.core.types.Expression<String> roadAddress, com.querydsl.core.types.Expression<Double> latitude, com.querydsl.core.types.Expression<Double> longitude, com.querydsl.core.types.Expression<String> memo, com.querydsl.core.types.Expression<String> category, com.querydsl.core.types.Expression<Boolean> isConfirmed) {
        super(ScheduleQueryDto.SchedulePlaceQueryDto.class, new Class<?>[]{long.class, String.class, String.class, double.class, double.class, String.class, String.class, boolean.class}, placeId, placeName, roadAddress, latitude, longitude, memo, category, isConfirmed);
    }

}

