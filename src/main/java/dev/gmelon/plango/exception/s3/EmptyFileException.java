package dev.gmelon.plango.exception.s3;

import dev.gmelon.plango.exception.InputInvalidException;

public class EmptyFileException extends InputInvalidException {

    private static final String MESSAGE = "빈 파일입니다.";

    public EmptyFileException() {
        super(MESSAGE, "file");
    }
}
