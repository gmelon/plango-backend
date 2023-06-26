package dev.gmelon.plango.web;

import dev.gmelon.plango.exception.UnauthenticatedException;
import dev.gmelon.plango.exception.UnauthorizedException;
import dev.gmelon.plango.exception.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // TODO 로깅

    // TODO validation error 어떻게 내릴지?
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto argumentNotValidException(MethodArgumentNotValidException exception) {
        return new ErrorResponseDto(exception.getMessage());
    }

    @ExceptionHandler(UnauthenticatedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponseDto unauthenticatedExceptionHandler(UnauthenticatedException exception) {
        return new ErrorResponseDto(exception.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponseDto unauthorizedExceptionHandler(UnauthorizedException exception) {
        return new ErrorResponseDto(exception.getMessage());
    }

    // TODO 사용자 예외로 분리?
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto runtimeExceptionHandler(RuntimeException exception) {
        return new ErrorResponseDto(exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto internalServerErrorHandler(Exception exception) {
        return new ErrorResponseDto("서버 오류가 발생했습니다. 관리자에게 문의하세요.");
    }
}
