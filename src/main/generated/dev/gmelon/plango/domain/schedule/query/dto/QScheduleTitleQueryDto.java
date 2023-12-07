package dev.gmelon.plango.domain.schedule.query.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * dev.gmelon.plango.domain.schedule.query.dto.QScheduleTitleQueryDto is a Querydsl Projection type for ScheduleTitleQueryDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QScheduleTitleQueryDto extends ConstructorExpression<ScheduleTitleQueryDto> {

    private static final long serialVersionUID = 43109986L;

    public QScheduleTitleQueryDto(com.querydsl.core.types.Expression<java.time.LocalDate> date, com.querydsl.core.types.Expression<String> title) {
        super(ScheduleTitleQueryDto.class, new Class<?>[]{java.time.LocalDate.class, String.class}, date, title);
    }

}

