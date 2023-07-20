package dev.gmelon.plango.config.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
public class LoginRequestDto {

    // TODO validation error message 작성하기
    @NotBlank
    private String emailOrNickname;

    @NotBlank
    private String password;

    @Builder
    public LoginRequestDto(String emailOrNickname, String password) {
        this.emailOrNickname = emailOrNickname;
        this.password = password;
    }

}
