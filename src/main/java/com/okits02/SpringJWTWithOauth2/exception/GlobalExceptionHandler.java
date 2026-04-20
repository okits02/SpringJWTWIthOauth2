package com.okits02.SpringJWTWithOauth2.exception;

import com.okits02.SpringJWTWithOauth2.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.Objects;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE = "min";

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(Exception e) {
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException e) {
        ErrorCode errorCode = e.getErrorCode();
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingDeniedException(AccessDeniedException e) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.UNAUTHORIZED.getCode());
        apiResponse.setMessage(ErrorCode.UNAUTHORIZED.getMessage());
        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getStatus())
                .body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse>  handleValidationException(MethodArgumentNotValidException e) {
        String enumKey = e.getFieldError().getDefaultMessage();

        ErrorCode errorCode = ErrorCode.valueOf(enumKey);
        Map<String, Object> attribute = null;
        try {
            errorCode = ErrorCode.valueOf(enumKey);
            var constraintViolation = e.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);
            attribute = constraintViolation.getConstraintDescriptor().getAttributes();
        } catch (IllegalArgumentException ex) {}
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(Objects.nonNull(attribute)
                ? mapAttribute(errorCode.getMessage(), attribute)
                : errorCode.getMessage());
        return  ResponseEntity.badRequest().body(apiResponse);
    }

    private String mapAttribute(String message, Map<String, Object> attributes) {
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));

        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
}
