package dev.gmelon.plango.domain.auth.exception;

import dev.gmelon.plango.global.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class EmailAuthenticationException extends PlangoException {
    private static final String MESSAGE = "이메일 인증이 완료되지 않았거나 만료되었습니다.";

    public EmailAuthenticationException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
