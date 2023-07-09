package dev.gmelon.plango.config.auth.exception;

import org.springframework.security.access.AccessDeniedException;

public class UnauthorizedException extends AccessDeniedException {

    private static final String MESSAGE = "권한이 없는 자원입니다.";

    public UnauthorizedException() {
        super(MESSAGE);
    }
}
