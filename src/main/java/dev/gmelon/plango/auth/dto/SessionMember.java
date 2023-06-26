package dev.gmelon.plango.auth.dto;

import dev.gmelon.plango.domain.member.Member;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SessionMember implements Serializable {

    public static final String SESSION_NAME = "member";

    private Long id;
    private String email;
    private String name;

    public static SessionMember of(Member member) {
        return SessionMember.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .build();
    }

    @Builder
    public SessionMember(Long id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }
}
