package dev.gmelon.plango.domain.s3.exception;

import dev.gmelon.plango.global.exception.InputInvalidException;

public class EmptyFileException extends InputInvalidException {

    private static final String MESSAGE = "빈 파일입니다.";

    public EmptyFileException() {
        super(MESSAGE, "file");
    }
}
