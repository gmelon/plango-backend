package dev.gmelon.plango.domain.schedule.dto;

import dev.gmelon.plango.domain.schedule.entity.Schedule;
import dev.gmelon.plango.domain.scheduleplace.dto.SchedulePlaceCreateRequestDto;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@NoArgsConstructor
@Getter
public class ScheduleCreateRequestDto {

    @NotBlank
    private String title;

    private String content;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    private List<Long> participantIds = new ArrayList<>();

    @Valid
    private List<SchedulePlaceCreateRequestDto> schedulePlaces = new ArrayList<>();

    @Builder
    public ScheduleCreateRequestDto(String title, String content, LocalDate date, LocalTime startTime, LocalTime endTime,
                                    List<Long> participantIds, List<SchedulePlaceCreateRequestDto> schedulePlaces) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.participantIds = participantIds;
        this.schedulePlaces = schedulePlaces;
    }

    public Schedule toEntity() {
        return Schedule.builder()
                .title(title)
                .content(content)
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

}
