package dev.gmelon.plango.global.config.auth.social.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class SocialAccountResponse {
    private String email;

    private String nickname;

    @Builder
    public SocialAccountResponse(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
    }
}
