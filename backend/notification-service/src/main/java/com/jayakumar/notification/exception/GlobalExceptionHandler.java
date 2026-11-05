package com.jayakumar.notification.exception;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotificationNotFoundException exception) {
        return createError(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HandlerMethodValidationException.class})
    public ResponseEntity<ApiError> handleValidationException(Exception exception) {
        return createError(HttpStatus.BAD_REQUEST, "Request validation failed");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiError> handleDuplicateKey(DuplicateKeyException exception) {
        return createError(HttpStatus.CONFLICT, "A notification with this eventId already exists");
    }

    private ResponseEntity<ApiError> createError(HttpStatus status, String message) {
        ApiError error = new ApiError(LocalDateTime.now(), status.value(), message);
        return ResponseEntity.status(status).body(error);
    }
}
