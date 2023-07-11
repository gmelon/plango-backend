package dev.gmelon.plango.service.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
public class PasswordChangeRequestDto {

    @NotBlank
    @Length(min = 8)
    private String previousPassword;

    @NotBlank
    @Length(min = 8)
    private String newPassword;

    @Builder
    public PasswordChangeRequestDto(String previousPassword, String newPassword) {
        this.previousPassword = previousPassword;
        this.newPassword = newPassword;
    }
}
