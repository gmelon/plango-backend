package dev.gmelon.plango.domain.member.exception;

import dev.gmelon.plango.global.exception.InputInvalidException;

public class PasswordMismatchException extends InputInvalidException {

    private static final String MESSAGE = "이전 비밀번호가 일치하지 않습니다.";

    public PasswordMismatchException() {
        super(MESSAGE, "previousPassword");
    }

}
