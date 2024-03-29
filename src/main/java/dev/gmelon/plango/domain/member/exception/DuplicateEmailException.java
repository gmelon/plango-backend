package dev.gmelon.plango.domain.member.exception;

import dev.gmelon.plango.global.exception.InputInvalidException;

public class DuplicateEmailException extends InputInvalidException {

    private static final String MESSAGE = "이미 존재하는 이메일입니다.";

    public DuplicateEmailException() {
        super(MESSAGE, "email");
    }
}
