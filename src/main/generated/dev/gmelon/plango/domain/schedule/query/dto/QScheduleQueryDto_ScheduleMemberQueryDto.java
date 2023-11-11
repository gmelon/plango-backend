package dev.gmelon.plango.domain.schedule.query.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * dev.gmelon.plango.domain.schedule.query.dto.QScheduleQueryDto_ScheduleMemberQueryDto is a Querydsl Projection type for ScheduleMemberQueryDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QScheduleQueryDto_ScheduleMemberQueryDto extends ConstructorExpression<ScheduleQueryDto.ScheduleMemberQueryDto> {

    private static final long serialVersionUID = 491370578L;

    public QScheduleQueryDto_ScheduleMemberQueryDto(com.querydsl.core.types.Expression<Long> memberId, com.querydsl.core.types.Expression<String> nickname, com.querydsl.core.types.Expression<String> profileImageUrl, com.querydsl.core.types.Expression<Boolean> isOwner, com.querydsl.core.types.Expression<Boolean> isAccepted, com.querydsl.core.types.Expression<Boolean> isCurrentMember) {
        super(ScheduleQueryDto.ScheduleMemberQueryDto.class, new Class<?>[]{long.class, String.class, String.class, boolean.class, boolean.class, boolean.class}, memberId, nickname, profileImageUrl, isOwner, isAccepted, isCurrentMember);
    }

}

