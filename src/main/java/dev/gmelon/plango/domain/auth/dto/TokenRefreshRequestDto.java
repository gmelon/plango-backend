package dev.gmelon.plango.domain.auth.dto;

import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TokenRefreshRequestDto {
    @NotBlank
    private String refreshToken;

    @Builder
    public TokenRefreshRequestDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
