package dev.gmelon.plango.domain.diary.exception;

import dev.gmelon.plango.global.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class DuplicateDiaryException extends PlangoException {

    private static final String MESSAGE = "이미 해당 일정에 기록이 존재합니다.";

    public DuplicateDiaryException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
