package dev.gmelon.plango.service.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
public class LoginRequestDto {

    // TODO validation error message 작성하기
    @NotBlank
    @Email // TODO 이메일 regex 작성
    private String email;

    @NotBlank
    private String password;

    @Builder
    public LoginRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

}
