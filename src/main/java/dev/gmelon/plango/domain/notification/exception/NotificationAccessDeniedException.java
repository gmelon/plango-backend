package dev.gmelon.plango.domain.notification.exception;

import org.springframework.security.access.AccessDeniedException;

public class NotificationAccessDeniedException extends AccessDeniedException {

    private static final String MESSAGE = "다른 사용자의 알림에 접근했습니다.";

    public NotificationAccessDeniedException() {
        super(MESSAGE);
    }
}
