package dev.gmelon.plango.domain.auth.dto;

import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.member.entity.MemberRole;
import dev.gmelon.plango.domain.member.entity.MemberType;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@NoArgsConstructor
@Getter
public class SignupRequestDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String tokenValue;

    @NotBlank
    @Length(min=8)
    private String password;

    @NotBlank
    private String nickname;

    private String profileImageUrl;

    @Builder
    public SignupRequestDto(String email, String tokenValue, String password, String nickname, String profileImageUrl) {
        this.email = email;
        this.tokenValue = tokenValue;
        this.password = password;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public void setEncodedPassword(String encodedPassword) {
        this.password = encodedPassword;
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
