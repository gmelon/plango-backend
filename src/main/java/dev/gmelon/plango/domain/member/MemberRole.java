package dev.gmelon.plango.domain.member;

import java.util.Arrays;

public enum MemberRole {
    ROLE_USER, ROLE_ADMIN;

    public static MemberRole parse(String roleString) {
        return Arrays.stream(values())
                .filter(role -> role.name().equalsIgnoreCase(roleString))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(roleString + "은 유효하지 않은 MemberRole 입니다."));
    }
}
