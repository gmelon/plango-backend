package dev.gmelon.plango.service.auth.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SendEmailTokenRequestDto {
    @Email
    @NotBlank
    private String email;

    @Builder
    public SendEmailTokenRequestDto(String email) {
        this.email = email;
    }
}
