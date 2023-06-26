package dev.gmelon.plango.exception.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

// TODO 패키지 고민
@NoArgsConstructor
@Getter
public class ErrorResponseDto {

    private String message;

    public ErrorResponseDto(String message) {
        this.message = message;
    }
}
