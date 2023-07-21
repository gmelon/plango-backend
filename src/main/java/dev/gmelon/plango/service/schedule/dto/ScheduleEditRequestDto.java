package dev.gmelon.plango.service.schedule.dto;

import dev.gmelon.plango.domain.schedule.ScheduleEditor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@Getter
public class ScheduleEditRequestDto {

    @NotBlank
    private String title;

    private String content;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    private Double latitude;

    private Double longitude;

    private String roadAddress;

    private String placeName;

    @Builder
    public ScheduleEditRequestDto(String title, String content, LocalDate date, LocalTime startTime, LocalTime endTime, Double latitude, Double longitude, String roadAddress, String placeName) {
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

    public ScheduleEditor toEditor() {
        return ScheduleEditor.builder()
                .title(title)
                .content(content)
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .latitude(latitude)
                .longitude(longitude)
                .roadAddress(roadAddress)
                .placeName(placeName)
                .build();
    }
}
