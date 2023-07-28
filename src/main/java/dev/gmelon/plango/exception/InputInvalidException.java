package dev.gmelon.plango.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InputInvalidException extends PlangoException {

    private final String field;

    public InputInvalidException(String message, String field) {
        super(message, HttpStatus.BAD_REQUEST);
        this.field = field;
    }

    public InputInvalidException(String message, String field, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST);
        this.field = field;
    }
}
