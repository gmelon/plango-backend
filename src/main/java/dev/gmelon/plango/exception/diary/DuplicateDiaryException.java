package dev.gmelon.plango.exception.diary;

import dev.gmelon.plango.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class DuplicateDiaryException extends PlangoException {

    private static final String MESSAGE = "이미 해당 일정에 기록이 존재합니다.";

    public DuplicateDiaryException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
