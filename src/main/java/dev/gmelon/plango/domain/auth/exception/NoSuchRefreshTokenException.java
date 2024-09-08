package dev.gmelon.plango.domain.auth.exception;

import dev.gmelon.plango.global.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class NoSuchRefreshTokenException extends PlangoException {
    private static final String MESSAGE = "유효하지 않은 Refresh Token 입니다.";

    public NoSuchRefreshTokenException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
