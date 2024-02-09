package dev.gmelon.plango.domain.schedule.repository.query.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ScheduleListQueryDto {

    private Long id;

    private String title;

    private String content;

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    private int memberCount;

    private Boolean isOwner;

    private Boolean isAccepted;

    @Setter
    private String confirmedPlaceNames;

    private Boolean done;

    @QueryProjection
    @Builder
    public ScheduleListQueryDto(Long id, String title, String content, LocalDate date, LocalTime startTime, LocalTime endTime, int memberCount, Boolean isOwner, Boolean isAccepted, Boolean done) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memberCount = memberCount;
        this.isOwner = isOwner;
        this.isAccepted = isAccepted;
        this.done = done;
    }
}
