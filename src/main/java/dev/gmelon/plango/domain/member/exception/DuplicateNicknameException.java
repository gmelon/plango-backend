package dev.gmelon.plango.domain.member.exception;

import dev.gmelon.plango.global.exception.InputInvalidException;

public class DuplicateNicknameException extends InputInvalidException {

    private static final String MESSAGE = "이미 존재하는 닉네임입니다.";

    public DuplicateNicknameException() {
        super(MESSAGE, "nickname");
    }
}
