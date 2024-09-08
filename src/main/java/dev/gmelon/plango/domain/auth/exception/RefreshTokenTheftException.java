package dev.gmelon.plango.domain.auth.exception;

import dev.gmelon.plango.global.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class RefreshTokenTheftException extends PlangoException {
    private static final String MESSAGE = "유효하지 않은 Refresh Token 입니다.";

    public RefreshTokenTheftException() {
        super(MESSAGE, HttpStatus.FORBIDDEN);
    }
}
