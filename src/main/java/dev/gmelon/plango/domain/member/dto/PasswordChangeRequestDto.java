package dev.gmelon.plango.domain.member.dto;

import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

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
