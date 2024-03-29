package dev.gmelon.plango.domain.member.dto;

import dev.gmelon.plango.domain.member.entity.MemberEditor;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@NoArgsConstructor
@Getter
public class MemberEditProfileRequestDto {

    @NotBlank
    private String nickname;

    private String profileImageUrl;

    @Length(max = 50)
    private String bio;

    @Builder
    public MemberEditProfileRequestDto(String nickname, String profileImageUrl, String bio) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.bio = bio;
    }

    public MemberEditor toEditor() {
        return MemberEditor.builder()
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .bio(bio)
                .build();
    }

}
