package dev.gmelon.plango.domain.schedule.dto;

import dev.gmelon.plango.domain.schedule.entity.ScheduleEditor;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@NoArgsConstructor
@Getter
public class ScheduleEditRequestDto {

    @NotBlank
    private String title;

    private String content;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    @Builder
    public ScheduleEditRequestDto(String title, String content, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public ScheduleEditor toEditor() {
        return ScheduleEditor.builder()
                .title(title)
                .content(content)
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }
}
