package com.krd.auth;

import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.SecretKey;

/**
 * Configuration properties for JWT token generation and validation.
 * <p>
 * Configure these properties in your application.yaml:
 * <pre>
 * spring:
 *   jwt:
 *     secret: ${JWT_SECRET}
 *     accessTokenExpiration: 900    # 15 minutes
 *     refreshTokenExpiration: 604800 # 7 days
 * </pre>
 *
 * @see JwtService
 */
@ConfigurationProperties(prefix = "spring.jwt")
@Data
public class JwtConfig {

    /**
     * The secret key used to sign JWT tokens.
     * <p>
     * <strong>SECURITY:</strong> This should be a strong, randomly generated secret
     * stored in environment variables, not hardcoded.
     * <p>
     * Minimum length: 32 characters (256 bits) for HS256 algorithm.
     */
    private String secret;

    /**
     * Expiration time for access tokens in seconds.
     * <p>
     * Access tokens are short-lived (typically 15 minutes = 900 seconds)
     * and sent with every API request.
     * <p>
     * Default: 900 seconds (15 minutes)
     */
    private int accessTokenExpiration;

    /**
     * Expiration time for refresh tokens in seconds.
     * <p>
     * Refresh tokens are long-lived (typically 7 days = 604800 seconds)
     * and used to obtain new access tokens.
     * <p>
     * Default: 604800 seconds (7 days)
     */
    private int refreshTokenExpiration;

    /**
     * Generates a SecretKey from the configured secret string.
     * <p>
     * This key is used for both signing and verifying JWT tokens.
     *
     * @return the secret key for JWT operations
     */
    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
