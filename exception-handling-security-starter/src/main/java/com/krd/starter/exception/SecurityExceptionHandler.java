package com.krd.starter.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

/**
 * Exception handler for Spring Security-related exceptions.
 * Extends the core exception-handling-starter with Security-specific handlers.
 *
 * <p>This handler provides standardized error responses for:
 * <ul>
 *     <li>Authentication failures (401)</li>
 *     <li>Authorization failures (403)</li>
 * </ul>
 *
 * <p><b>Usage:</b>
 * Add the {@code exception-handling-security-starter} dependency to your microservice
 * that uses Spring Security. This starter automatically registers this handler via
 * Spring Boot auto-configuration.
 *
 * <p><b>Dependency Setup:</b>
 * <pre>
 * implementation 'com.krd:exception-handling-security-starter:1.0.0'
 * </pre>
 *
 * @see ErrorResponse (from exception-handling-starter)
 * @see GlobalExceptionHandler (from exception-handling-starter)
 * @since 1.0.0
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE) // Same precedence as GlobalExceptionHandler
public class SecurityExceptionHandler {

    /**
     * Handles bad credentials (failed login attempts).
     * Returns 401 Unauthorized.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex,
            WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Invalid email or password")
                .path(getRequestPath(request))
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    /**
     * Handles authorization failures (access denied to resources).
     * Returns 403 Forbidden.
     *
     * Handles both AccessDeniedException (Spring Security 5.x) and
     * AuthorizationDeniedException (Spring Security 6.x).
     */
    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            Exception ex,
            WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message("You do not have permission to access this resource")
                .path(getRequestPath(request))
                .build();

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    /**
     * Extracts the request path from WebRequest for inclusion in error responses.
     */
    private String getRequestPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
