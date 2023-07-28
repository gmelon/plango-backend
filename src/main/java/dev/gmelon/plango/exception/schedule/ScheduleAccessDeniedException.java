package dev.gmelon.plango.exception.schedule;

import org.springframework.security.access.AccessDeniedException;

public class ScheduleAccessDeniedException extends AccessDeniedException {

    private static final String MESSAGE = "다른 사용자의 일정에 접근했습니다.";

    public ScheduleAccessDeniedException() {
        super(MESSAGE);
    }
}
