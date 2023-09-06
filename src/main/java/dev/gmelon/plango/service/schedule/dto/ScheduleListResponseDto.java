package dev.gmelon.plango.service.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.gmelon.plango.domain.schedule.query.dto.ScheduleListQueryDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@Getter
public class ScheduleListResponseDto {

    private Long id;

    private String title;

    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "Asia/Seoul")
    private LocalTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "Asia/Seoul")
    private LocalTime endTime;

    private int memberCount;

    private Boolean isOwner;

    private Double latitude;

    private Double longitude;

    private String roadAddress;

    private String placeName;

    private Boolean isDone;

    public static ScheduleListResponseDto from(ScheduleListQueryDto queryDto) {
        return ScheduleListResponseDto.builder()
                .id(queryDto.getId())
                .title(queryDto.getTitle())
                .content(queryDto.getContent())
                .date(queryDto.getDate())
                .startTime(queryDto.getStartTime())
                .endTime(queryDto.getEndTime())
                .memberCount(queryDto.getMemberCount())
                .isOwner(queryDto.getIsOwner())
                .latitude(queryDto.getLatitude())
                .longitude(queryDto.getLongitude())
                .roadAddress(queryDto.getRoadAddress())
                .placeName(queryDto.getPlaceName())
                .isDone(queryDto.getDone())
                .build();
    }

    @Builder
    public ScheduleListResponseDto(Long id, String title, String content, LocalDate date, LocalTime startTime, LocalTime endTime,
                                   int memberCount, Boolean isOwner,
                                   Double latitude, Double longitude, String roadAddress, String placeName, Boolean isDone) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memberCount = memberCount;
        this.isOwner = isOwner;
        this.latitude = latitude;
        this.longitude = longitude;
        this.roadAddress = roadAddress;
        this.placeName = placeName;
        this.isDone = isDone;
    }
}
