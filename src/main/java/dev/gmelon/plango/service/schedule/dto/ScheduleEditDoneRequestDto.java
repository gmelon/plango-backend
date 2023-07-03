package dev.gmelon.plango.service.schedule.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
public class ScheduleEditDoneRequestDto {

    @NotNull
    private Boolean isDone;

    public ScheduleEditDoneRequestDto(Boolean isDone) {
        this.isDone = isDone;
    }
}
