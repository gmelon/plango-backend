package dev.gmelon.plango.exception.schedule.place;

import dev.gmelon.plango.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class NoSuchSchedulePlaceException extends PlangoException {

    private static final String MESSAGE = "존재하지 않는 일정 장소입니다.";

    public NoSuchSchedulePlaceException() {
        super(MESSAGE, HttpStatus.NOT_FOUND);
    }
}
