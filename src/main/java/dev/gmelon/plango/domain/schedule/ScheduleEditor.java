package dev.gmelon.plango.domain.schedule;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class ScheduleEditor {

    private String title;
    private String content;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    @Builder
    public ScheduleEditor(String title, String content, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
