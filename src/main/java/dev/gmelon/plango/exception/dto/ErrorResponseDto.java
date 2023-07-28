package dev.gmelon.plango.exception.dto;

import dev.gmelon.plango.exception.ErrorMessages;
import dev.gmelon.plango.exception.PlangoException;
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

    public static ErrorResponseDto from(PlangoException exception) {
        return ErrorResponseDto.builder()
                .message(exception.getMessage())
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
}
