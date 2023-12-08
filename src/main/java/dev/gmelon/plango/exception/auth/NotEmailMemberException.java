package dev.gmelon.plango.exception.auth;

import dev.gmelon.plango.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class NotEmailMemberException extends PlangoException {
    private static final String MESSAGE = "소셜 계정으로는 요청하신 작업을 수행할 수 없습니다. 해당 소셜 서비스에 문의해 주세요.";

    public NotEmailMemberException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
