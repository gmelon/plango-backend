package dev.gmelon.plango.service.schedule.dto;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.schedule.Schedule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class ScheduleCreateRequestDto {

    @NotBlank
    private String title;

    private String content;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    private String location;

    @Builder
    public ScheduleCreateRequestDto(String title, String content, LocalDateTime startTime, LocalDateTime endTime, String location) {
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
    }

    public Schedule toEntity(Member member) {
        return Schedule.builder()
                .title(title)
                .content(content)
                .startTime(startTime)
                .endTime(endTime)
                .location(location)
                .member(member)
                .build();
    }
}
