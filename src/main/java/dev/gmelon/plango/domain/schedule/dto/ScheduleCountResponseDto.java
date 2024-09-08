package dev.gmelon.plango.domain.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.gmelon.plango.domain.schedule.repository.query.dto.ScheduleCountQueryDto;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
