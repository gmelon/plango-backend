package dev.gmelon.plango.service.auth.dto;

import dev.gmelon.plango.config.auth.social.dto.SocialAccountResponse;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.member.MemberType;
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
