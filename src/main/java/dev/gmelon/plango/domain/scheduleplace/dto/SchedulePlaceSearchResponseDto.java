package dev.gmelon.plango.domain.scheduleplace.dto;

import dev.gmelon.plango.domain.scheduleplace.entity.SchedulePlace;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SchedulePlaceSearchResponseDto {

    private Long scheduleId;

    private String scheduleTitle;

    private String placeName;

    @Builder
    public SchedulePlaceSearchResponseDto(Long scheduleId, String scheduleTitle, String placeName) {
        this.scheduleId = scheduleId;
        this.scheduleTitle = scheduleTitle;
        this.placeName = placeName;
    }

    public static SchedulePlaceSearchResponseDto from(SchedulePlace schedulePlace) {
        return SchedulePlaceSearchResponseDto.builder()
                .scheduleId(schedulePlace.getSchedule().getId())
                .scheduleTitle(schedulePlace.getSchedule().getTitle())
                .placeName(schedulePlace.getPlaceName())
                .build();
    }
}
