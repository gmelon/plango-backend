package dev.gmelon.plango.exception.diary;

import org.springframework.security.access.AccessDeniedException;

public class DiaryAccessDeniedException extends AccessDeniedException {

    private static final String MESSAGE = "다른 사용자의 기록에 접근했습니다.";

    public DiaryAccessDeniedException() {
        super(MESSAGE);
    }
}
