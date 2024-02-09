package dev.gmelon.plango.domain.fcm.exception;

import dev.gmelon.plango.global.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class NoSuchFirebaseCloudMessageTokenException extends PlangoException {

    private static final String MESSAGE = "존재하지 않는 FirebaseCloudMessage 토큰입니다.";

    public NoSuchFirebaseCloudMessageTokenException() {
        super(MESSAGE, HttpStatus.NOT_FOUND);
    }
}
