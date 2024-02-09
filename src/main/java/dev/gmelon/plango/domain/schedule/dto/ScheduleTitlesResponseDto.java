package dev.gmelon.plango.domain.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ScheduleTitlesResponseDto {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate date;

    private List<String> titles = new ArrayList<>();

    @Builder
    public ScheduleTitlesResponseDto(LocalDate date, List<String> titles) {
        this.date = date;
        this.titles = titles;
    }
}
