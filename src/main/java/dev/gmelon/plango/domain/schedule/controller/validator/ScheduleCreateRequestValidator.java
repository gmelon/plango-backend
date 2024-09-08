package dev.gmelon.plango.domain.schedule.controller.validator;

import dev.gmelon.plango.domain.schedule.dto.ScheduleCreateRequestDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ScheduleCreateRequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(ScheduleCreateRequestDto.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ScheduleCreateRequestDto requestDto = (ScheduleCreateRequestDto) target;

        if (checkStartOrEndTimeIsNull(requestDto)) {
            return;
        }

        if (requestDto.getEndTime().isBefore(requestDto.getStartTime())) {
            errors.reject("endTimeIsBeforeStartTime", "종료 시각은 시작 시각보다 뒤여야 합니다.");
        }
    }

    private boolean checkStartOrEndTimeIsNull(ScheduleCreateRequestDto requestDto) {
        return requestDto.getStartTime() == null || requestDto.getEndTime() == null;
    }

}
