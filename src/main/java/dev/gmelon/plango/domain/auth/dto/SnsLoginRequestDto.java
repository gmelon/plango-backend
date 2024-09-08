package dev.gmelon.plango.domain.auth.dto;

import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.member.entity.MemberRole;
import dev.gmelon.plango.domain.member.entity.MemberType;
import dev.gmelon.plango.global.config.auth.social.dto.SocialAccountResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SnsLoginRequestDto {
    @NotNull
    private MemberType memberType;

    @NotBlank
    private String token;

    @Builder
    public SnsLoginRequestDto(MemberType memberType, String token) {
        this.memberType = memberType;
        this.token = token;
    }

    public Member toEntity(SocialAccountResponse socialAccountResponse) {
        return Member.builder()
                .email(socialAccountResponse.getEmail())
                .nickname(socialAccountResponse.getNickname())
                .password("")
                .role(MemberRole.ROLE_USER)
                .type(memberType)
                .build();
    }
}
