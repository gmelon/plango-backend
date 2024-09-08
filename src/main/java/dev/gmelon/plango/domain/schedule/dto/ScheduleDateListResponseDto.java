package dev.gmelon.plango.domain.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.gmelon.plango.domain.schedule.repository.query.dto.ScheduleListQueryDto;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ScheduleDateListResponseDto {

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

    private String confirmedPlaceNames;

    private Boolean isDone;

    public static ScheduleDateListResponseDto from(ScheduleListQueryDto queryDto) {
        return ScheduleDateListResponseDto.builder()
                .id(queryDto.getId())
                .title(queryDto.getTitle())
                .content(queryDto.getContent())
                .date(queryDto.getDate())
                .startTime(queryDto.getStartTime())
                .endTime(queryDto.getEndTime())
                .memberCount(queryDto.getMemberCount())
                .isOwner(queryDto.getIsOwner())
                .confirmedPlaceNames(queryDto.getConfirmedPlaceNames())
                .isDone(queryDto.getDone())
                .build();
    }

    @Builder
    public ScheduleDateListResponseDto(Long id, String title, String content, LocalDate date,
                                       LocalTime startTime, LocalTime endTime, int memberCount, Boolean isOwner, String confirmedPlaceNames, Boolean isDone) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memberCount = memberCount;
        this.isOwner = isOwner;
        this.confirmedPlaceNames = confirmedPlaceNames;
        this.isDone = isDone;
    }
}
