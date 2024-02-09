package dev.gmelon.plango.domain.schedule.exception;

import dev.gmelon.plango.global.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class NoSuchScheduleMemberException extends PlangoException {

    private static final String MESSAGE = "일정에 참여하지 않는 회원입니다.";

    public NoSuchScheduleMemberException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
