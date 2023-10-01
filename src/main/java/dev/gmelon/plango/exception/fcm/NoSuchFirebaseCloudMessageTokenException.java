package dev.gmelon.plango.exception.fcm;

import dev.gmelon.plango.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class NoSuchFirebaseCloudMessageTokenException extends PlangoException {

    private static final String MESSAGE = "존재하지 않는 FirebaseCloudMessage 토큰입니다.";

    public NoSuchFirebaseCloudMessageTokenException() {
        super(MESSAGE, HttpStatus.NOT_FOUND);
    }
}
