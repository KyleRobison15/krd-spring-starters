package com.krd.starter.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Data;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wrapper class for JWT claims and secret key.
 * <p>
 * This class provides convenient methods to access user information
 * from JWT tokens and check token expiration.
 *
 * @see JwtService
 * @see JwtAuthenticationFilter
 */
@Data
public class Jwt {

    private final Claims claims;
    private final SecretKey secretKey;

    /**
     * Checks if this JWT token has expired.
     *
     * @return true if the token has expired, false otherwise
     */
    public boolean isExpired() {
        return claims.getExpiration().before(new Date());
    }

    /**
     * Extracts the user ID from the JWT subject claim.
     *
     * @return the user's ID
     */
    public Long getUserId() {
        return Long.valueOf(claims.getSubject());
    }

    /**
     * Extracts the user's email from the JWT claims.
     *
     * @return the user's email
     */
    public String getEmail() {
        return claims.get("email", String.class);
    }

    /**
     * Extracts the user's username from the JWT claims.
     *
     * @return the user's username
     */
    public String getUsername() {
        return claims.get("username", String.class);
    }

    /**
     * Extracts the user's first name from the JWT claims.
     *
     * @return the user's first name
     */
    public String getFirstName() {
        return claims.get("firstName", String.class);
    }

    /**
     * Extracts the user's last name from the JWT claims.
     *
     * @return the user's last name
     */
    public String getLastName() {
        return claims.get("lastName", String.class);
    }

    /**
     * Extracts the user's roles from the JWT claims.
     * <p>
     * Roles are stored as a comma-separated string in the token
     * and parsed back into a Set.
     *
     * @return a set of role names
     */
    public Set<String> getRoles() {
        String rolesString = claims.get("roles", String.class);
        if (rolesString == null || rolesString.isEmpty()) {
            return Set.of();
        }
        return Arrays.stream(rolesString.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    /**
     * Checks if the user account is enabled.
     *
     * @return true if the account is enabled, false otherwise
     */
    public boolean isEnabled() {
        return claims.get("enabled", Boolean.class);
    }

    /**
     * Converts this JWT back to a compact string representation.
     * <p>
     * This is used to generate the token string that's sent to clients.
     *
     * @return the JWT token as a string
     */
    @Override
    public String toString() {
        return Jwts.builder()
                .claims(claims)
                .signWith(secretKey)
                .compact();
    }
}
