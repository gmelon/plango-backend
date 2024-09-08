package dev.gmelon.plango.domain.schedule.exception;

import dev.gmelon.plango.global.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class NoSuchScheduleException extends PlangoException {

    private static final String MESSAGE = "존재하지 않는 일정입니다.";

    public NoSuchScheduleException() {
        super(MESSAGE, HttpStatus.NOT_FOUND);
    }
}
