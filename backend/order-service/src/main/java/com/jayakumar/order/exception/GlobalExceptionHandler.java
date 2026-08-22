package com.jayakumar.order.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(
            OrderNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse response =
                new ErrorResponse(
                        LocalDateTime.now(),
                        404,
                        "Not Found",
                        ex.getMessage(),
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }
    @ExceptionHandler(DownstreamServiceException.class)
    public ResponseEntity<ErrorResponse> handleDownstreamService(
        DownstreamServiceException ex,
        HttpServletRequest request) {

    ErrorResponse response =
            new ErrorResponse(
                    LocalDateTime.now(),
                    503,
                    "Service Unavailable",
                    ex.getMessage(),
                    request.getRequestURI()
            );

    return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(response);
}
}