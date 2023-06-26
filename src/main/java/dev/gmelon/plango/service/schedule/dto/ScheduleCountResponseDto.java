package dev.gmelon.plango.service.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
public class ScheduleCountResponseDto implements Comparable<ScheduleCountResponseDto>{

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate date;

    private int count;

    @Builder
    public ScheduleCountResponseDto(LocalDate date, int count) {
        this.date = date;
        this.count = count;
    }

    @Override
    public int compareTo(ScheduleCountResponseDto o) {
        return this.date.getDayOfMonth() - o.date.getDayOfMonth();
    }
}
