package dev.gmelon.plango.exception.diary;

import dev.gmelon.plango.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class NoSuchDiaryException extends PlangoException {

    private static final String MESSAGE = "존재하지 않는 기록입니다.";

    public NoSuchDiaryException() {
        super(MESSAGE, HttpStatus.NOT_FOUND);
    }
}
