package dev.gmelon.plango.exception.schedule;

import dev.gmelon.plango.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class NoSuchScheduleMemberException extends PlangoException {

    private static final String MESSAGE = "일정에 참여하지 않는 회원입니다.";

    public NoSuchScheduleMemberException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
