package com.krd.starter.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler that provides consistent error responses across all microservices.
 * All errors follow a standardized structure defined by {@link ErrorResponse}.
 *
 * <p>This handler processes common exceptions that apply to any Spring Boot microservice:
 * <ul>
 *     <li>Validation errors (400)</li>
 *     <li>Malformed requests (400)</li>
 *     <li>Unsupported media types (415)</li>
 *     <li>Unexpected server errors (500)</li>
 * </ul>
 *
 * <p><b>Spring Security Exception Handling:</b>
 * If your microservice uses Spring Security, add the {@code exception-handling-security-starter}
 * dependency to handle authentication (401) and authorization (403) exceptions.
 *
 * <p><b>Domain-specific exceptions</b> (e.g., UserNotFoundException, ChatNotFoundException)
 * should be handled in microservice-specific {@code @ControllerAdvice} classes with higher precedence.
 *
 * <p><b>Extending this handler:</b>
 * <pre>
 * &#64;ControllerAdvice
 * &#64;Order(Ordered.HIGHEST_PRECEDENCE) // Higher precedence than this global handler
 * public class MyServiceExceptionHandler {
 *
 *     &#64;ExceptionHandler(MyCustomException.class)
 *     public ResponseEntity&lt;ErrorResponse&gt; handleMyException(
 *             MyCustomException ex, WebRequest request) {
 *         // Handle domain-specific exception
 *     }
 * }
 * </pre>
 *
 * @see ErrorResponse
 * @since 1.0.0
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE) // Lower precedence - allows microservices to override
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles validation errors from @Valid and @Validated annotations.
     * Returns 400 Bad Request with field-level error details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed for one or more fields")
                .path(getRequestPath(request))
                .errors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles malformed JSON or unreadable request bodies.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex,
            WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Malformed JSON request or invalid request body")
                .path(getRequestPath(request))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles unsupported media type errors (e.g., sending text/plain when application/json is expected).
     * Returns 415 Unsupported Media Type.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .error(HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase())
                .message("Unsupported media type. This endpoint requires application/json")
                .path(getRequestPath(request))
                .build();

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(errorResponse);
    }

    /**
     * Handles IllegalStateException (e.g., trying to delete last admin, remove own admin role).
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex,
            WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(getRequestPath(request))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles IllegalArgumentException (e.g., incorrect password, disabled account).
     * Returns appropriate status code based on the error message.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {

        HttpStatus status;
        String message;

        // Check for specific error scenarios
        if (ex.getMessage() != null && ex.getMessage().contains("Cannot generate token for disabled user")) {
            status = HttpStatus.FORBIDDEN;
            message = "Your account has been disabled. Please contact support.";
        } else if (ex.getMessage() != null && ex.getMessage().contains("Current password is incorrect")) {
            status = HttpStatus.BAD_REQUEST;
            message = "Current password is incorrect";
        } else {
            status = HttpStatus.BAD_REQUEST;
            message = ex.getMessage() != null ? ex.getMessage() : "Invalid request";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(getRequestPath(request))
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Catch-all handler for unexpected errors (e.g., database errors, external API failures).
     * Returns 500 Internal Server Error.
     *
     * Microservices can override this by creating their own Exception handler with higher precedence
     * to customize behavior for specific uncaught exceptions (e.g., OpenAI API errors).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedErrors(
            Exception ex,
            WebRequest request) {

        // Log the full exception for debugging
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred. Please try again later.")
                .path(getRequestPath(request))
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    /**
     * Extracts the request path from WebRequest for inclusion in error responses.
     */
    private String getRequestPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
