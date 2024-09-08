package dev.gmelon.plango.domain.auth.dto;

import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@NoArgsConstructor
@Getter
public class LoginRequestDto {

    @NotBlank
    private String emailOrNickname;

    @NotBlank
    private String password;

    @Builder
    public LoginRequestDto(String emailOrNickname, String password) {
        this.emailOrNickname = emailOrNickname;
        this.password = password;
    }

    public Authentication toAuthentication() {
        return UsernamePasswordAuthenticationToken.unauthenticated(emailOrNickname.trim(), password.trim());
    }

}
