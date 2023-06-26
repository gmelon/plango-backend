package dev.gmelon.plango.exception;

public class UnauthorizedException extends RuntimeException {

    private static final String MESSAGE = "권한이 없는 자원입니다.";

    public UnauthorizedException() {
        super(MESSAGE);
    }
}
