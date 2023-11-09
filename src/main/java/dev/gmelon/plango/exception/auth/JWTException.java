package dev.gmelon.plango.exception.auth;

import dev.gmelon.plango.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class JWTException extends PlangoException {
    public JWTException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
