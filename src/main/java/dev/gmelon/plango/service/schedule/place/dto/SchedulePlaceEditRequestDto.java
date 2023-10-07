package dev.gmelon.plango.service.schedule.place.dto;

import dev.gmelon.plango.domain.schedule.place.SchedulePlaceEditor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
public class SchedulePlaceEditRequestDto {

    @NotBlank
    private String placeName;

    private String roadAddress;

    private Double latitude;

    private Double longitude;

    private String memo;

    @Length(max = 10)
    private String category;

    @Builder
    public SchedulePlaceEditRequestDto(String placeName, String roadAddress, Double latitude, Double longitude, String memo, String category) {
        this.placeName = placeName;
        this.roadAddress = roadAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.memo = memo;
        this.category = category;
    }

    public SchedulePlaceEditor toEditor() {
        return SchedulePlaceEditor.builder()
                .placeName(placeName)
                .roadAddress(roadAddress)
                .latitude(latitude)
                .longitude(longitude)
                .memo(memo)
                .category(category)
                .build();
    }
}
