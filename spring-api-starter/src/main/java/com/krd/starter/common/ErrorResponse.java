package com.krd.starter.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error response structure for all API errors.
 * Follows RFC 7807 (Problem Details for HTTP APIs) principles.
 *
 * Example response for validation error:
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Validation failed",
 *   "path": "/chats/123/messages",
 *   "errors": [
 *     {
 *       "field": "prompt",
 *       "message": "Prompt cannot be empty",
 *       "rejectedValue": ""
 *     }
 *   ]
 * }
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Omit null fields from JSON response
public class ErrorResponse {

    /**
     * Timestamp when the error occurred
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code (e.g., 400, 404, 500)
     */
    private int status;

    /**
     * HTTP status text (e.g., "Bad Request", "Not Found")
     */
    private String error;

    /**
     * Human-readable error message
     */
    private String message;

    /**
     * The request path that caused the error
     */
    private String path;

    /**
     * Field-level validation errors (only for validation failures)
     */
    private List<FieldError> errors;

    /**
     * Represents a single field validation error
     */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FieldError {
        /**
         * Name of the field that failed validation
         */
        private String field;

        /**
         * Validation error message
         */
        private String message;

        /**
         * The value that was rejected (optional, for debugging)
         */
        private Object rejectedValue;
    }
}
