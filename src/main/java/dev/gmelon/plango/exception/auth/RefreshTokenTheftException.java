package dev.gmelon.plango.exception.auth;

import dev.gmelon.plango.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class RefreshTokenTheftException extends PlangoException {
    private static final String MESSAGE = "유효하지 않은 Refresh Token 입니다.";

    public RefreshTokenTheftException() {
        super(MESSAGE, HttpStatus.FORBIDDEN);
    }
}
