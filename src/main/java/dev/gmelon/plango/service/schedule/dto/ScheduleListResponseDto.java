package dev.gmelon.plango.service.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.gmelon.plango.domain.schedule.Schedule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@Getter
public class ScheduleListResponseDto {

    private Long id;

    private String title;

    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "Asia/Seoul")
    private LocalTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "Asia/Seoul")
    private LocalTime endTime;

    private Double latitude;

    private Double longitude;

    private String roadAddress;

    private String placeName;

    private Boolean isDone;

    public static ScheduleListResponseDto from(Schedule schedule) {
        return ScheduleListResponseDto.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .content(schedule.getContent())
                .date(schedule.getDate())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .latitude(schedule.getLatitude())
                .longitude(schedule.getLongitude())
                .roadAddress(schedule.getRoadAddress())
                .placeName(schedule.getPlaceName())
                .isDone(schedule.isDone())
                .build();
    }

    @Builder
    public ScheduleListResponseDto(Long id, String title, String content, LocalDate date, LocalTime startTime, LocalTime endTime, Double latitude, Double longitude, String roadAddress, String placeName, Boolean isDone) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.roadAddress = roadAddress;
        this.placeName = placeName;
        this.isDone = isDone;
    }
}
