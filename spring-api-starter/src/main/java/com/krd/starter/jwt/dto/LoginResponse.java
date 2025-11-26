package com.krd.starter.jwt.dto;

import com.krd.starter.jwt.Jwt;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response DTO containing both access and refresh tokens after successful login.
 * <p>
 * Typically, the access token is returned in the response body,
 * while the refresh token is set as an HttpOnly cookie for security.
 *
 * @see com.krd.starter.jwt.dto.JwtResponse
 */
@AllArgsConstructor
@Getter
public class LoginResponse {
    /**
     * Short-lived access token (typically 15 minutes).
     */
    private Jwt accessToken;

    /**
     * Long-lived refresh token (typically 7 days).
     */
    private Jwt refreshToken;
}
