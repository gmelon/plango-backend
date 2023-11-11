package dev.gmelon.plango.domain.schedule.query;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * dev.gmelon.plango.domain.schedule.query.QScheduleQueryRepositoryImpl_SchedulePlaceLikeQueryDto is a Querydsl Projection type for SchedulePlaceLikeQueryDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QScheduleQueryRepositoryImpl_SchedulePlaceLikeQueryDto extends ConstructorExpression<ScheduleQueryRepositoryImpl.SchedulePlaceLikeQueryDto> {

    private static final long serialVersionUID = -1502253788L;

    public QScheduleQueryRepositoryImpl_SchedulePlaceLikeQueryDto(com.querydsl.core.types.Expression<Long> placeId, com.querydsl.core.types.Expression<Long> memberId) {
        super(ScheduleQueryRepositoryImpl.SchedulePlaceLikeQueryDto.class, new Class<?>[]{long.class, long.class}, placeId, memberId);
    }

}

