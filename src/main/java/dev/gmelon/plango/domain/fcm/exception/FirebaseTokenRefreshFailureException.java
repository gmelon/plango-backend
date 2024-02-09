package dev.gmelon.plango.domain.fcm.exception;

import dev.gmelon.plango.global.exception.InternalServerException;

public class FirebaseTokenRefreshFailureException extends InternalServerException {

    private static final String MESSAGE = "Firebase token refresh에 실패했습니다.";

    public FirebaseTokenRefreshFailureException() {
        super(MESSAGE);
    }
}
