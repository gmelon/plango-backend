package dev.gmelon.plango.exception.schedule;

import dev.gmelon.plango.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class ScheduleNotAcceptedException extends PlangoException {

    private static final String MESSAGE = "아직 수락하지 않은 일정입니다. 주어진 작업을 수행할 수 없습니다.";

    public ScheduleNotAcceptedException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
