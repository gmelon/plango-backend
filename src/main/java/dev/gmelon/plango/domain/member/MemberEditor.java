package dev.gmelon.plango.domain.member;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberEditor {

    private String nickname;
    private String profileImageUrl;

    @Builder
    public MemberEditor(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }
}
