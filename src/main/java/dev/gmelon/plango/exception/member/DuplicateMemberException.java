package dev.gmelon.plango.exception.member;

import dev.gmelon.plango.exception.InputInvalidException;

public class DuplicateMemberException extends InputInvalidException {
    private static final String MESSAGE = "동일한 이메일을 사용하는 회원이 이미 존재합니다.";

    public DuplicateMemberException() {
        super(MESSAGE, "email");
    }
}
