package dev.gmelon.plango.domain.schedule.query.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ScheduleTitleQueryDto {
    private LocalDate date;

    private String title;

    @QueryProjection
    @Builder
    public ScheduleTitleQueryDto(LocalDate date, String title) {
        this.date = date;
        this.title = title;
    }
}
