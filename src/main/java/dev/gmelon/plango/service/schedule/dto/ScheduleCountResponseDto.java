package dev.gmelon.plango.service.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.gmelon.plango.domain.schedule.query.dto.ScheduleCountQueryDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
public class ScheduleCountResponseDto{

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate date;

    private long doneCount;

    private long totalCount;

    @Builder
    public ScheduleCountResponseDto(LocalDate date, long doneCount, long totalCount) {
        this.date = date;
        this.doneCount = doneCount;
        this.totalCount = totalCount;
    }

    public static ScheduleCountResponseDto from(ScheduleCountQueryDto queryDto) {
        return ScheduleCountResponseDto.builder()
                .date(queryDto.getDate())
                .doneCount(queryDto.getDoneCount())
                .totalCount(queryDto.getTotalCount())
                .build();
    }
}
