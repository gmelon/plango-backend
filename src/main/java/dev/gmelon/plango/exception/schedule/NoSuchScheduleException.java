package dev.gmelon.plango.exception.schedule;

import dev.gmelon.plango.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class NoSuchScheduleException extends PlangoException {

    private static final String MESSAGE = "존재하지 않는 일정입니다.";

    public NoSuchScheduleException() {
        super(MESSAGE, HttpStatus.NOT_FOUND);
    }
}
