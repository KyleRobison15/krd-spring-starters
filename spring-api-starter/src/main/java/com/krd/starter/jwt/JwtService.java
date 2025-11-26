package com.krd.starter.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.stream.Collectors;

/**
 * Service for generating and parsing JWT tokens.
 * <p>
 * This service handles the creation of access and refresh tokens,
 * as well as parsing and validating tokens from requests.
 * <p>
 * Usage example:
 * <pre>
 * {@code
 * @Service
 * public class AuthService {
 *     private final JwtService jwtService;
 *
 *     public String login(User user) {
 *         Jwt accessToken = jwtService.generateAccessToken(user);
 *         return accessToken.toString();
 *     }
 * }
 * }
 * </pre>
 *
 * @see Jwt
 * @see JwtConfig
 * @see JwtUser
 */
@AllArgsConstructor
public class JwtService {

    private final JwtConfig config;

    /**
     * Generates a short-lived access token for the given user.
     * <p>
     * Access tokens are typically valid for 15 minutes and are sent
     * with every API request in the Authorization header.
     *
     * @param user the user to generate a token for
     * @return a JWT access token
     * @throws IllegalArgumentException if user is null or disabled
     */
    public Jwt generateAccessToken(JwtUser user) {
        validateUser(user);
        return generateToken(user, config.getAccessTokenExpiration());
    }

    /**
     * Generates a long-lived refresh token for the given user.
     * <p>
     * Refresh tokens are typically valid for 7 days and are used
     * to obtain new access tokens without requiring re-authentication.
     * <p>
     * Refresh tokens should be stored securely (e.g., in HttpOnly cookies).
     *
     * @param user the user to generate a token for
     * @return a JWT refresh token
     * @throws IllegalArgumentException if user is null or disabled
     */
    public Jwt generateRefreshToken(JwtUser user) {
        validateUser(user);
        return generateToken(user, config.getRefreshTokenExpiration());
    }

    /**
     * Validates that a user is eligible for token generation.
     *
     * @param user the user to validate
     * @throws IllegalArgumentException if user is null or disabled
     */
    private void validateUser(JwtUser user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (!user.isEnabled()) {
            throw new IllegalArgumentException("Cannot generate token for disabled user");
        }
    }

    /**
     * Generates a JWT token with the specified expiration time.
     *
     * @param user the user to generate a token for
     * @param tokenExpiration expiration time in seconds
     * @return a JWT token
     */
    private Jwt generateToken(JwtUser user, long tokenExpiration) {
        // Convert roles Set to comma-separated string for storage in JWT
        String rolesString = user.getRoles().stream()
                .collect(Collectors.joining(","));

        var claims = Jwts.claims()
                .subject(user.getId().toString())
                .add("email", user.getEmail())
                .add("username", user.getUsername())
                .add("firstName", user.getFirstName())
                .add("lastName", user.getLastName())
                .add("roles", rolesString)
                .add("enabled", user.isEnabled())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * tokenExpiration))
                .build();

        return new Jwt(claims, config.getSecretKey());
    }

    /**
     * Parses and validates a JWT token string.
     * <p>
     * This method verifies the token's signature and expiration.
     * Invalid or expired tokens return null rather than throwing exceptions.
     *
     * @param token the JWT token string (without "Bearer " prefix)
     * @return a Jwt object if valid, null otherwise
     */
    public Jwt parseToken(String token) {
        try {
            var claims = getClaims(token);
            return new Jwt(claims, config.getSecretKey());
        } catch (JwtException e) {
            // Invalid token (signature mismatch, malformed, etc.)
            return null;
        }
    }

    /**
     * Extracts and verifies claims from a JWT token string.
     *
     * @param token the JWT token string
     * @return the verified claims
     * @throws JwtException if the token is invalid
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(config.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
