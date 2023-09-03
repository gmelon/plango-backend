package dev.gmelon.plango.exception.schedule;

import dev.gmelon.plango.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class DuplicateScheduleMemberException extends PlangoException {

    private static final String MESSAGE = "이미 일정에 참여하고 있는 회원입니다.";

    public DuplicateScheduleMemberException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
