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
    private Double latitude;
    private Double longitude;
    private String roadAddress;
    private String placeName;

    @Builder
    public ScheduleEditor(String title, String content, LocalDate date, LocalTime startTime, LocalTime endTime, Double latitude, Double longitude, String roadAddress, String placeName) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.roadAddress = roadAddress;
        this.placeName = placeName;
    }
}
