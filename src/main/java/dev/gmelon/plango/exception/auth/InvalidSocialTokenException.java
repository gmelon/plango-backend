package dev.gmelon.plango.exception.auth;

import dev.gmelon.plango.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class InvalidSocialTokenException extends PlangoException {
    private static final String MESSAGE = "소셜 로그인 정보가 올바르지 않습니다.";

    public InvalidSocialTokenException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST);
    }

    public InvalidSocialTokenException(Throwable cause) {
        super(MESSAGE, cause, HttpStatus.BAD_REQUEST);
    }
}
