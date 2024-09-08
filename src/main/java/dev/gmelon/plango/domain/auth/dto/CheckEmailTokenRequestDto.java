package dev.gmelon.plango.domain.auth.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CheckEmailTokenRequestDto {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String tokenValue;

    @Builder
    public CheckEmailTokenRequestDto(String email, String tokenValue) {
        this.email = email;
        this.tokenValue = tokenValue;
    }
}
