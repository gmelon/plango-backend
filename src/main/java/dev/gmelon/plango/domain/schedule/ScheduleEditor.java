package dev.gmelon.plango.domain.schedule;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ScheduleEditor {

    private String title;
    private String content;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Builder
    public ScheduleEditor(String title, String content, LocalDateTime startTime, LocalDateTime endTime) {
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
