package dev.gmelon.plango.domain.scheduleplace.dto;

import dev.gmelon.plango.domain.schedule.entity.Schedule;
import dev.gmelon.plango.domain.scheduleplace.entity.SchedulePlace;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@NoArgsConstructor
@Getter
public class SchedulePlaceCreateRequestDto {

    @NotBlank
    private String placeName;

    private String roadAddress;

    private Double latitude;

    private Double longitude;

    private String memo;

    @Length(max = 10)
    private String category;

    @Builder
    public SchedulePlaceCreateRequestDto(String placeName, String roadAddress, Double latitude, Double longitude, String memo, String category) {
        this.placeName = placeName;
        this.roadAddress = roadAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.memo = memo;
        this.category = category;
    }

    public SchedulePlace toEntity(Schedule schedule) {
        return SchedulePlace.builder()
                .placeName(placeName)
                .roadAddress(roadAddress)
                .latitude(latitude)
                .longitude(longitude)
                .memo(memo)
                .category(category)
                .schedule(schedule)
                .confirmed(false)
                .build();
    }
}
