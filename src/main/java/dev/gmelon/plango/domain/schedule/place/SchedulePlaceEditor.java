package dev.gmelon.plango.domain.schedule.place;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SchedulePlaceEditor {

    private String placeName;
    private String roadAddress;
    private Double latitude;
    private Double longitude;
    private String memo;
    private String category;

    @Builder
    public SchedulePlaceEditor(String placeName, String roadAddress, Double latitude, Double longitude, String memo, String category) {
        this.placeName = placeName;
        this.roadAddress = roadAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.memo = memo;
        this.category = category;
    }
}
