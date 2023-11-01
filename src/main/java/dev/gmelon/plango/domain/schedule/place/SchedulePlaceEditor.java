package dev.gmelon.plango.domain.schedule.place;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SchedulePlaceEditor {

    private String memo;
    private String category;

    @Builder
    public SchedulePlaceEditor(String memo, String category) {
        this.memo = memo;
        this.category = category;
    }
}
