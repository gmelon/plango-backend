package dev.gmelon.plango.service.member.dto;

import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PasswordResetRequestDto {
    @NotBlank
    private String email;

    @Builder
    public PasswordResetRequestDto(String email) {
        this.email = email;
    }
}
