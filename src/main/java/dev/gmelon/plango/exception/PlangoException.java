package dev.gmelon.plango.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class PlangoException extends RuntimeException {

    private final HttpStatus status;

    public PlangoException() {
        super(ErrorMessages.INTERNAL_SERVER_ERROR_MESSAGE);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public PlangoException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public PlangoException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }
}
