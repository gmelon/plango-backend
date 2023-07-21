package dev.gmelon.plango.service.member.dto;

import dev.gmelon.plango.domain.member.MemberEditor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
public class MemberEditProfileRequestDto {

    @NotBlank
    private String nickname;

    private String profileImageUrl;

    @Builder
    public MemberEditProfileRequestDto(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public MemberEditor toEditor() {
        return MemberEditor.builder()
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .build();
    }

}
