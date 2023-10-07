package dev.gmelon.plango.domain.schedule.query.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ScheduleCountQueryDto {

    private LocalDate date;

    private int doneCount;

    private int totalCount;

    @QueryProjection
    @Builder
    public ScheduleCountQueryDto(LocalDate date, int doneCount, int totalCount) {
        this.date = date;
        this.doneCount = doneCount;
        this.totalCount = totalCount;
    }
}
