package dev.gmelon.plango.domain.diary.exception;

import dev.gmelon.plango.global.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class NoSuchDiaryException extends PlangoException {

    private static final String MESSAGE = "존재하지 않는 기록입니다.";

    public NoSuchDiaryException() {
        super(MESSAGE, HttpStatus.NOT_FOUND);
    }
}
