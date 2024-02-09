package dev.gmelon.plango.domain.member.exception;

import dev.gmelon.plango.global.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class NoSuchMemberException extends PlangoException {

    private static final String MESSAGE = "존재하지 않는 회원입니다.";

    public NoSuchMemberException() {
        super(MESSAGE, HttpStatus.NOT_FOUND);
    }
}
