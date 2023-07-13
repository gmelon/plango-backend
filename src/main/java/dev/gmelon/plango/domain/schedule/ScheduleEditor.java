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
    private Double latitude;
    private Double longitude;
    private String roadAddress;
    private String placeName;

    @Builder
    public ScheduleEditor(String title, String content, LocalDateTime startTime, LocalDateTime endTime, Double latitude, Double longitude, String roadAddress, String placeName) {
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.roadAddress = roadAddress;
        this.placeName = placeName;
    }
}
