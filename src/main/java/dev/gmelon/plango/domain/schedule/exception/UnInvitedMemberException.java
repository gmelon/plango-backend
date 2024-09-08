package dev.gmelon.plango.domain.schedule.exception;

import dev.gmelon.plango.global.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class UnInvitedMemberException extends PlangoException {

    private static final String MESSAGE = "존재하지 않거나 초대받지 않은 일정입니다.";

    public UnInvitedMemberException() {
        super(MESSAGE, HttpStatus.NOT_FOUND);
    }
}
