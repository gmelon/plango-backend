package dev.gmelon.plango.web;

import dev.gmelon.plango.exception.InputInvalidException;
import dev.gmelon.plango.exception.PlangoException;
import dev.gmelon.plango.exception.dto.ErrorResponseDto;
import dev.gmelon.plango.exception.dto.InputInvalidErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

// TODO 로깅
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InputInvalidException.class)
    public ResponseEntity<InputInvalidErrorResponseDto> inputInvalidExceptionHandler(InputInvalidException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(InputInvalidErrorResponseDto.from(exception));
    }

    @ExceptionHandler(PlangoException.class)
    public ResponseEntity<ErrorResponseDto> plangoExceptionHandler(PlangoException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(ErrorResponseDto.from(exception));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> authenticationExceptionHandler(AuthenticationException exception) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponseDto.unAuthorized());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> accessDeniedExceptionHandler(AccessDeniedException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseDto.notFound());
    }

    // TODO 스프링 내부 Default 예외 처리

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<InputInvalidErrorResponseDto> constraintViolationExceptionHandler(ConstraintViolationException exception) {
        return ResponseEntity
                .badRequest()
                .body(InputInvalidErrorResponseDto.from(exception));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<InputInvalidErrorResponseDto> argumentNotValidException(MethodArgumentNotValidException exception) {
        return ResponseEntity
                .badRequest()
                .body(InputInvalidErrorResponseDto.from(exception));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> unhandledExceptionHandler(Exception exception) {
        return ResponseEntity
                .internalServerError()
                .body(ErrorResponseDto.internalSeverError());
    }
}
