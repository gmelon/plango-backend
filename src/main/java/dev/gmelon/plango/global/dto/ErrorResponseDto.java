package dev.gmelon.plango.global.dto;

import dev.gmelon.plango.global.exception.ErrorMessages;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ErrorResponseDto {

    private String message;

    @Builder
    public ErrorResponseDto(String message) {
        this.message = message;
    }

    public static ErrorResponseDto from(Exception exception) {
        return ErrorResponseDto.builder()
                .message(exception.getLocalizedMessage())
                .build();
    }

    public static ErrorResponseDto internalSeverError() {
        return ErrorResponseDto.builder()
                .message(ErrorMessages.INTERNAL_SERVER_ERROR_MESSAGE)
                .build();
    }

    public static ErrorResponseDto notFound() {
        return ErrorResponseDto.builder()
                .message(ErrorMessages.NOT_FOUND_ERROR_MESSAGE)
                .build();
    }

    public static ErrorResponseDto unAuthorized() {
        return ErrorResponseDto.builder()
                .message(ErrorMessages.UNAUTHORIZED_ERROR_MESSAGE)
                .build();
    }

    public static ErrorResponseDto loginFailed() {
        return ErrorResponseDto.builder()
                .message(ErrorMessages.LOGIN_FAILURE_ERROR_MESSAGE)
                .build();
    }
}
