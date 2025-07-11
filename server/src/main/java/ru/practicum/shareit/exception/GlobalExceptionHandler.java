package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        log.warn("Not Found Error: {}", ex.getMessage(), ex);
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        log.warn("Conflict Error: {}", ex.getMessage(), ex);
        return buildErrorResponse(ex, HttpStatus.CONFLICT, "CONFLICT");
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        log.warn("Forbidden Error: {}", ex.getMessage(), ex);
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOtherExceptions(Exception ex) {
        log.error("Internal Server Error: {}", ex.getMessage(), ex);
        return buildErrorResponse("Внутренняя ошибка сервера",
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR");
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(Exception ex, HttpStatus status, String errorCode) {
        return buildErrorResponse(ex.getMessage(), status, errorCode);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status, String errorCode) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(
                        errorCode,
                        message,
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        log.warn("Validation Exception: {}", ex.getMessage(), ex);
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
    }

}