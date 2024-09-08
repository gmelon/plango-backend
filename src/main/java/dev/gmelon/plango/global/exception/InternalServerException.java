package dev.gmelon.plango.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class InternalServerException extends PlangoException {

    public InternalServerException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
