package dev.gmelon.plango.service.schedule.dto;

import dev.gmelon.plango.domain.schedule.ScheduleEditor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class ScheduleEditRequestDto {

    @NotBlank
    private String title;

    private String content;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    @Builder
    public ScheduleEditRequestDto(String title, String content, LocalDateTime startTime, LocalDateTime endTime) {
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public ScheduleEditor toScheduleEditor() {
        return ScheduleEditor.builder()
                .title(title)
                .content(content)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }
}
