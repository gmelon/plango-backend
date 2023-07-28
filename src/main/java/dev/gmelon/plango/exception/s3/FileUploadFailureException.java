package dev.gmelon.plango.exception.s3;

import dev.gmelon.plango.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class FileUploadFailureException extends PlangoException {

    private static final String MESSAGE = "파일 업로드에 실패했습니다.";

    public FileUploadFailureException() {
        super(MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
