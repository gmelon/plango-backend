package dev.gmelon.plango.web;

import dev.gmelon.plango.exception.InputInvalidException;
import dev.gmelon.plango.exception.InternalServerException;
import dev.gmelon.plango.exception.PlangoException;
import dev.gmelon.plango.exception.dto.ErrorResponseDto;
import dev.gmelon.plango.exception.dto.InputInvalidErrorResponseDto;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;

import static org.springframework.http.HttpStatus.*;

// TODO 로깅
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorResponseDto> internalServerExceptionHandler(InternalServerException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(ErrorResponseDto.internalSeverError());
    }

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
                .status(UNAUTHORIZED)
                .body(ErrorResponseDto.unAuthorized());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> accessDeniedExceptionHandler(AccessDeniedException exception) {
        return ResponseEntity
                .status(NOT_FOUND)
                .body(ErrorResponseDto.notFound());
    }

    @ExceptionHandler({
            ServletRequestBindingException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestPartException.class
    })
    public ResponseEntity<ErrorResponseDto> badRequestExceptionHandler(Exception exception) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(ErrorResponseDto.from(exception));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<InputInvalidErrorResponseDto> missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException exception) {
        return ResponseEntity
                .badRequest()
                .body(InputInvalidErrorResponseDto.from(exception));
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<InputInvalidErrorResponseDto> typeMismatchExceptionHandler(TypeMismatchException exception) {
        return ResponseEntity
                .badRequest()
                .body(InputInvalidErrorResponseDto.from(exception));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<InputInvalidErrorResponseDto> constraintViolationExceptionHandler(ConstraintViolationException exception) {
        return ResponseEntity
                .badRequest()
                .body(InputInvalidErrorResponseDto.from(exception));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<InputInvalidErrorResponseDto> argumentNotValidExceptionHandler(MethodArgumentNotValidException exception) {
        return ResponseEntity
                .badRequest()
                .body(InputInvalidErrorResponseDto.from(exception));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<InputInvalidErrorResponseDto> bindExceptionHandler(BindException exception) {
        return ResponseEntity
                .badRequest()
                .body(InputInvalidErrorResponseDto.from(exception));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponseDto> notFoundExceptionHandler(Exception exception) {
        return ResponseEntity
                .status(NOT_FOUND)
                .body(ErrorResponseDto.notFound());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> methodNotAllowedExceptionHandler(Exception exception) {
        return ResponseEntity
                .status(METHOD_NOT_ALLOWED)
                .body(ErrorResponseDto.from(exception));
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponseDto> notAcceptableExceptionHandler(Exception exception) {
        return ResponseEntity
                .status(NOT_ACCEPTABLE)
                .body(ErrorResponseDto.from(exception));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> unSupportedMediaTypeExceptionHandler(Exception exception) {
        return ResponseEntity
                .status(UNSUPPORTED_MEDIA_TYPE)
                .body(ErrorResponseDto.from(exception));
    }

    @ExceptionHandler({
            MissingPathVariableException.class,
            ConversionNotSupportedException.class,
            HttpMessageNotWritableException.class,
    })
    public ResponseEntity<ErrorResponseDto> internalServerErrorExceptionHandler(Exception exception) {
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDto.from(exception));
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<ErrorResponseDto> serviceUnavailableExceptionHandler(Exception exception) {
        return ResponseEntity
                .status(SERVICE_UNAVAILABLE)
                .body(ErrorResponseDto.from(exception));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> unhandledExceptionHandler(Exception exception) {
        return ResponseEntity
                .internalServerError()
                .body(ErrorResponseDto.internalSeverError());
    }
}
