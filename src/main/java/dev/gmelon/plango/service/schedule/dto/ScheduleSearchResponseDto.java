package dev.gmelon.plango.service.schedule.dto;

import dev.gmelon.plango.domain.schedule.Schedule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
public class ScheduleSearchResponseDto {

    private Long id;

    private String title;

    private String content;

    private LocalDate date;

    @Builder
    public ScheduleSearchResponseDto(Long id, String title, String content, LocalDate date) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
    }

    public static ScheduleSearchResponseDto from(Schedule schedule) {
        return ScheduleSearchResponseDto.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .content(schedule.getContent())
                .date(schedule.getDate())
                .build();
    }
}
