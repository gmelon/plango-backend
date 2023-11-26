package dev.gmelon.plango.service.auth.dto;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.member.MemberType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
public class SignupRequestDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Length(min=8)
    private String password;

    @NotBlank
    private String nickname;

    private String profileImageUrl;

    @Builder
    public SignupRequestDto(String email, String password, String nickname, String profileImageUrl) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Member toEntity() {
        return Member.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .role(MemberRole.ROLE_USER)
                .type(MemberType.EMAIL)
                .termsAccepted(true)
                .build();
    }
}
