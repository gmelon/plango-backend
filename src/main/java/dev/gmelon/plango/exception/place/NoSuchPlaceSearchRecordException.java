package dev.gmelon.plango.exception.place;

import dev.gmelon.plango.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class NoSuchPlaceSearchRecordException extends PlangoException {

    private static final String MESSAGE = "존재하지 않는 검색 기록입니다.";

    public NoSuchPlaceSearchRecordException() {
        super(MESSAGE, HttpStatus.NOT_FOUND);
    }
}
