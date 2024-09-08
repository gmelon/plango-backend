package dev.gmelon.plango.domain.place.exception;

import org.springframework.security.access.AccessDeniedException;

public class PlaceSearchRecordAccessDeniedException extends AccessDeniedException {

    private static final String MESSAGE = "다른 사용자의 일정에 접근했습니다.";

    public PlaceSearchRecordAccessDeniedException() {
        super(MESSAGE);
    }
}
