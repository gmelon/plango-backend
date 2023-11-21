package dev.gmelon.plango.exception.auth;

import dev.gmelon.plango.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class JWTException extends PlangoException {
    public JWTException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

    public static JWTException unSupportedToken() {
        return new JWTException("지원하지 않는 토큰입니다.");
    }

    public static JWTException invalidToken() {
        return new JWTException("유효하지 않은 토큰입니다.");
    }
}
