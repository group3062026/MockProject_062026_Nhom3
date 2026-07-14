package com.nguyenquyen.mockproject_062026_group3.exception;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        log.warn("AppException: [Code: {}, Message: {}]", errorCode.getCode(), errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError() != null ? exception.getFieldError().getDefaultMessage() : "";
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        String message = errorCode.getMessage();

        try {
            if (enumKey != null && !enumKey.isEmpty()) {
                errorCode = ErrorCode.valueOf(enumKey);
                message = errorCode.getMessage();
            }
        } catch (IllegalArgumentException e) {
            // Nếu message trong @Valid không phải là tên enum của ErrorCode thì lấy trực tiếp message đó
            FieldError fieldError = exception.getFieldError();
            if (fieldError != null && fieldError.getDefaultMessage() != null) {
                message = fieldError.getField() + ": " + fieldError.getDefaultMessage();
            }
        }

        log.warn("Validation error: {}", message);
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.error(errorCode.getCode(), message));
    }

    @ExceptionHandler(value = {
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleRequestParameterException(Exception exception) {
        ErrorCode errorCode = ErrorCode.INVALID_PARAMETER;
        log.warn("Parameter error: {}", exception.getMessage());
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.error(errorCode.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUncategorizedException(Exception exception) {
        log.error("Uncategorized Exception: ", exception);
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage()));
    }
}
