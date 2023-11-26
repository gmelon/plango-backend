package dev.gmelon.plango.service.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TermsAcceptedResponseDto {
    private boolean termsAccepted;

    @Builder
    public TermsAcceptedResponseDto(boolean termsAccepted) {
        this.termsAccepted = termsAccepted;
    }
}
