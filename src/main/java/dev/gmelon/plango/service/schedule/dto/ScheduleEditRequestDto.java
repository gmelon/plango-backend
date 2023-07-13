package dev.gmelon.plango.service.schedule.dto;

import dev.gmelon.plango.domain.schedule.ScheduleEditor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class ScheduleEditRequestDto {

    @NotBlank
    private String title;

    private String content;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    private Double latitude;

    private Double longitude;

    private String roadAddress;

    private String placeName;

    @Builder
    public ScheduleEditRequestDto(String title, String content, LocalDateTime startTime, LocalDateTime endTime, Double latitude, Double longitude, String roadAddress, String placeName) {
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.roadAddress = roadAddress;
        this.placeName = placeName;
    }

    public ScheduleEditor toScheduleEditor() {
        return ScheduleEditor.builder()
                .title(title)
                .content(content)
                .startTime(startTime)
                .endTime(endTime)
                .latitude(latitude)
                .longitude(longitude)
                .roadAddress(roadAddress)
                .placeName(placeName)
                .build();
    }
}
