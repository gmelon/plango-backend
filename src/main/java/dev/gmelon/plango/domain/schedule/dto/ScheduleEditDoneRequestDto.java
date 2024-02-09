package dev.gmelon.plango.domain.schedule.dto;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ScheduleEditDoneRequestDto {

    @NotNull
    private Boolean isDone;

    public ScheduleEditDoneRequestDto(Boolean isDone) {
        this.isDone = isDone;
    }
}
