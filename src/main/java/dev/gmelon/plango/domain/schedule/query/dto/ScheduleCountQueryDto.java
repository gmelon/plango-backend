package dev.gmelon.plango.domain.schedule.query.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Getter
public class ScheduleCountQueryDto {

    private LocalDate date;

    private long doneCount;

    private long totalCount;

}
