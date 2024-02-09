package dev.gmelon.plango.domain.fcm.exception;

import org.springframework.security.access.AccessDeniedException;

public class FirebaseCloudMessageTokenAccessDeniedException extends AccessDeniedException {

    private static final String MESSAGE = "다른 사용자의 FirebaseCloudMessage 토큰에 접근했습니다.";

    public FirebaseCloudMessageTokenAccessDeniedException() {
        super(MESSAGE);
    }
}
