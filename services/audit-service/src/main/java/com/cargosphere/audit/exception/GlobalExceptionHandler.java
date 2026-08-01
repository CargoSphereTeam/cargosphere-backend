package com.cargosphere.audit.exception;

import com.cargosphere.audit.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuditLogNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            AuditLogNotFoundException exception
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(InvalidAuditQueryException.class)
    public ResponseEntity<ErrorResponse>
    handleInvalidAuditQuery(
            InvalidAuditQueryException exception
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> validationErrors =
                new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        validationErrors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                validationErrors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse>
    handleConstraintViolation(
            ConstraintViolationException exception
    ) {
        Map<String, String> validationErrors =
                new LinkedHashMap<>();

        exception.getConstraintViolations()
                .forEach(violation ->
                        validationErrors.put(
                                violation
                                        .getPropertyPath()
                                        .toString(),
                                violation.getMessage()
                        )
                );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                validationErrors
        );
    }

    @ExceptionHandler(
            MethodArgumentTypeMismatchException.class
    )
    public ResponseEntity<ErrorResponse>
    handleTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid value for parameter: "
                        + exception.getName(),
                null
        );
    }

    @ExceptionHandler(
            HttpMessageNotReadableException.class
    )
    public ResponseEntity<ErrorResponse>
    handleUnreadableRequest(
            HttpMessageNotReadableException exception
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Malformed request body or invalid enum value",
                null
        );
    }

    @ExceptionHandler(
            DataIntegrityViolationException.class
    )
    public ResponseEntity<ErrorResponse>
    handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "Audit data violates a database constraint",
                null
        );
    }

    @ExceptionHandler(
            AuthorizationDeniedException.class
    )
    public ResponseEntity<ErrorResponse>
    handleAuthorizationDenied(
            AuthorizationDeniedException exception
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "Access denied",
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>
    handleUnexpected(
            Exception exception
    ) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                null
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            Map<String, String> validationErrors
    ) {
        ErrorResponse response =
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(status.value())
                        .error(
                                status.getReasonPhrase()
                        )
                        .message(message)
                        .validationErrors(
                                validationErrors
                        )
                        .build();

        return ResponseEntity
                .status(status)
                .body(response);
    }
}