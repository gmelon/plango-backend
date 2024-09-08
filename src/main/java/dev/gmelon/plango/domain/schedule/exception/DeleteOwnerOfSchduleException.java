package dev.gmelon.plango.domain.schedule.exception;

import dev.gmelon.plango.global.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class DeleteOwnerOfSchduleException extends PlangoException {

    private static final String MESSAGE = "일정의 소유자는 삭제할 수 없습니다.";

    public DeleteOwnerOfSchduleException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
