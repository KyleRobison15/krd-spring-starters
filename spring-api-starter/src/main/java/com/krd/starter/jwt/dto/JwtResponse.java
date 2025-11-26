package com.krd.starter.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response DTO containing a JWT token string.
 * <p>
 * This is typically returned from login and token refresh endpoints.
 * <p>
 * Example JSON response:
 * <pre>
 * {
 *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
 * }
 * </pre>
 *
 * @see LoginResponse
 */
@AllArgsConstructor
@Data
public class JwtResponse {
    /**
     * The JWT token as a string.
     */
    private String token;
}
