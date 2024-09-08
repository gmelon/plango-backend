package dev.gmelon.plango.domain.notification.exception;

import dev.gmelon.plango.global.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class NoSuchNotificationException extends PlangoException {

    private static final String MESSAGE = "존재하지 않는 알림입니다.";

    public NoSuchNotificationException() {
        super(MESSAGE, HttpStatus.NOT_FOUND);
    }
}
