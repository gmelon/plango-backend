package dev.gmelon.plango.domain.schedule.query.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@Builder
@Getter
public class ScheduleListQueryDto {

    private Long id;

    private String title;

    private String content;

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    private int memberCount;

    private Double latitude;

    private Double longitude;

    private String roadAddress;

    private String placeName;

    private Boolean done;

}
