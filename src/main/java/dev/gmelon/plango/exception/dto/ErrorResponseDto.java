package dev.gmelon.plango.exception.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// TODO 패키지 고민
@NoArgsConstructor
@Getter
public class ErrorResponseDto {

    private String message;

    @Builder
    public ErrorResponseDto(String message) {
        this.message = message;
    }
}
