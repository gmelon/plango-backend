package dev.gmelon.plango.config.auth.dto;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRole;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String userNameAttributeKey;
    private String email;
    private String nickname;
    private String profileImageUrl;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String userNameAttributeKey, String email, String nickname,
                           String profileImageUrl) {
        this.attributes = attributes;
        this.userNameAttributeKey = userNameAttributeKey;
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    @SuppressWarnings("unchecked")
    public static OAuthAttributes of(String registrationId, String userNameAttributeKey,
                                     Map<String, Object> attributes) {
        return ofKakao("email", (Map<String, Object>) attributes.get(userNameAttributeKey));
    }

    public static OAuthAttributes ofKakao(String userNameAttributeKey,
                                          Map<String, Object> attributes) {
        @SuppressWarnings("unchecked")
        Map<String, Object> profiles = (Map<String, Object>) attributes.get("profile");

        return OAuthAttributes.builder()
                .email(String.valueOf(attributes.get(userNameAttributeKey)))
                .nickname(String.valueOf(profiles.get("nickname")))
                .profileImageUrl(String.valueOf(profiles.get("profile_image_url")))
                .attributes(attributes)
                .userNameAttributeKey(userNameAttributeKey)
                .build();
    }

    public Member toEntity() {
        return Member.builder()
                .email(email)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .role(MemberRole.ROLE_USER)
                .password("")
                .build();
    }
}
