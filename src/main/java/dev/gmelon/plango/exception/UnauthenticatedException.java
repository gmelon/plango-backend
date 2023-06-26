package dev.gmelon.plango.exception;

public class UnauthenticatedException extends RuntimeException {

    private static final String MESSAGE = "로그인이 필요합니다.";

    public UnauthenticatedException() {
        super(MESSAGE);
    }
}
