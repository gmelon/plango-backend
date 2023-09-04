package dev.gmelon.plango.exception.dto;

import dev.gmelon.plango.exception.InputInvalidException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.beans.TypeMismatchException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

@NoArgsConstructor
@Getter
public class InputInvalidErrorResponseDto {

    private String field;
    private String message;

    @Builder
    public InputInvalidErrorResponseDto(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public static InputInvalidErrorResponseDto from(InputInvalidException exception) {
        return InputInvalidErrorResponseDto.builder()
                .field(exception.getField())
                .message(exception.getMessage())
                .build();
    }

    public static InputInvalidErrorResponseDto from(MissingServletRequestParameterException exception) {
        return InputInvalidErrorResponseDto.builder()
                .field(exception.getParameterName())
                .message(exception.getLocalizedMessage())
                .build();
    }

    public static InputInvalidErrorResponseDto from(TypeMismatchException exception) {
        return InputInvalidErrorResponseDto.builder()
                .field(exception.getPropertyName())
                .message(exception.getLocalizedMessage())
                .build();
    }

    public static InputInvalidErrorResponseDto from(MethodArgumentNotValidException exception) {
        if (exception.hasFieldErrors()) {
            return fieldErrorResponse(exception);
        }
        return objectErrorResponse(exception);
    }

    private static InputInvalidErrorResponseDto fieldErrorResponse(MethodArgumentNotValidException exception) {
        FieldError firstFieldError = exception.getFieldErrors().get(0);
        return InputInvalidErrorResponseDto.builder()
                .field(firstFieldError.getField())
                .message(firstFieldError.getDefaultMessage())
                .build();
    }

    private static InputInvalidErrorResponseDto objectErrorResponse(MethodArgumentNotValidException exception) {
        ObjectError firstError = exception.getAllErrors().get(0);
        return InputInvalidErrorResponseDto.builder()
                .field(firstError.getObjectName())
                .message(firstError.getDefaultMessage())
                .build();
    }

    public static InputInvalidErrorResponseDto from(ConstraintViolationException exception) {
        ConstraintViolation<?> constraintViolation = exception.getConstraintViolations().iterator().next();
        String field = getField(constraintViolation);

        return InputInvalidErrorResponseDto.builder()
                .field(field)
                .message(constraintViolation.getMessage())
                .build();
    }

    public static InputInvalidErrorResponseDto from(BindException exception) {
        FieldError firstFieldError = exception.getFieldErrors().get(0);

        return InputInvalidErrorResponseDto.builder()
                .field(firstFieldError.getField())
                .message(firstFieldError.getDefaultMessage())
                .build();
    }

    private static String getField(ConstraintViolation<?> constraintViolation) {
        PathImpl propertyPath = (PathImpl) constraintViolation.getPropertyPath();
        return propertyPath.getLeafNode().asString();
    }
}
