package dev.gmelon.plango.exception.schedule;

import dev.gmelon.plango.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class NoOwnerOfScheduleException extends PlangoException {

    private static final String MESSAGE = "일정의 소유자가 아닙니다. 주어진 작업을 수행할 수 없습니다.";

    public NoOwnerOfScheduleException() {
        super(MESSAGE, HttpStatus.FORBIDDEN);
    }
}
