package dev.gmelon.plango.exception.fcm;

import dev.gmelon.plango.exception.InternalServerException;

public class FirebaseTokenRefreshFailureException extends InternalServerException {

    private static final String MESSAGE = "Firebase token refresh에 실패했습니다.";

    public FirebaseTokenRefreshFailureException() {
        super(MESSAGE);
    }
}
