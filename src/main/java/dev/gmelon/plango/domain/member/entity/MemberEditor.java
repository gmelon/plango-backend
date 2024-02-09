package dev.gmelon.plango.domain.member.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberEditor {

    private String nickname;
    private String profileImageUrl;
    private String bio;

    @Builder
    public MemberEditor(String nickname, String profileImageUrl, String bio) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.bio = bio;
    }
}
