package com.krd.starter.jwt;

import com.krd.starter.jwt.dto.JwtResponse;
import com.krd.starter.jwt.dto.LoginRequest;
import com.krd.starter.user.BaseUser;
import com.krd.starter.user.BaseUserMapper;
import com.krd.starter.user.dto.BaseUserDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

/**
 * Abstract base controller for authentication endpoints.
 * <p>
 * Provides complete REST API for authentication out of the box:
 * - POST   /auth/login                  - Login with email/password (sets refresh token cookie)
 * - POST   /auth/refresh                - Refresh access token using cookie
 * - GET    /auth/me                     - Get current authenticated user
 * - POST   /auth/revoke-refresh-token   - Revoke refresh token (logout)
 * <p>
 * Consumer applications should extend this class:
 * <pre>
 * {@code
 * @RestController
 * @RequestMapping("/auth")
 * @Tag(name = "Auth")
 * public class AuthController extends BaseAuthController<User, UserDto> {
 *     public AuthController(JwtConfig jwtConfig,
 *                           UserMapper userMapper,
 *                           AuthService authService) {
 *         super(jwtConfig, userMapper, authService);
 *     }
 *     // Add custom endpoints here if needed
 * }
 * }
 * </pre>
 *
 * @param <T> The concrete user entity type extending BaseUser
 * @param <D> The concrete user DTO type extending BaseUserDto
 */
public abstract class BaseAuthController<T extends BaseUser, D extends BaseUserDto> {

    protected final JwtConfig jwtConfig;
    protected final BaseUserMapper<T, D> userMapper;
    protected final BaseAuthService<T> authService;

    protected BaseAuthController(JwtConfig jwtConfig,
                                BaseUserMapper<T, D> userMapper,
                                BaseAuthService<T> authService) {
        this.jwtConfig = jwtConfig;
        this.userMapper = userMapper;
        this.authService = authService;
    }

    /**
     * Login with email and password.
     * <p>
     * Sets an HTTP-only cookie containing the refresh token.
     * Returns the access token in the response body.
     *
     * @param request  Login request with email and password
     * @param response HTTP response for setting the refresh token cookie
     * @return JwtResponse containing the access token
     */
    @PostMapping("/login")
    public JwtResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        var loginResponse = authService.login(request);
        var refreshToken = loginResponse.getRefreshToken().toString();

        var cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true); // Cannot be accessed by JavaScript
        cookie.setPath("/auth/refresh"); // Only sent to /auth/refresh endpoint
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration()); // 7 days
        cookie.setSecure(true); // Only sent over HTTPS
        response.addCookie(cookie);

        // Return the access token in the response body
        return new JwtResponse(loginResponse.getAccessToken().toString());
    }

    /**
     * Refresh the access token using the refresh token from the cookie.
     *
     * @param refreshToken The refresh token from the cookie
     * @return JwtResponse containing the new access token
     */
    @PostMapping("/refresh")
    public JwtResponse refresh(@CookieValue(value = "refreshToken") String refreshToken) {
        var accessToken = authService.refreshAccessToken(refreshToken);
        return new JwtResponse(accessToken.toString());
    }

    /**
     * Get the currently authenticated user.
     *
     * @return User DTO or 404 if not found
     */
    @GetMapping("/me")
    public ResponseEntity<D> me() {
        var user = authService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        var userDto = userMapper.toDto(user);
        return ResponseEntity.ok(userDto);
    }

    /**
     * Revoke the refresh token by clearing the HttpOnly cookie.
     * <p>
     * Note: The current access token remains valid until expiration (~15 minutes).
     * The user will not be able to obtain new access tokens after this call.
     * <p>
     * For complete logout, the client should:
     * 1. Call this endpoint to revoke the refresh token
     * 2. Delete the access token from client memory
     * 3. Redirect to login page
     *
     * @param response HTTP response for clearing the refresh token cookie
     * @return 204 No Content
     */
    @PostMapping("/revoke-refresh-token")
    public ResponseEntity<Void> revokeRefreshToken(HttpServletResponse response) {
        // Clear the refresh token cookie by setting MaxAge to 0
        var cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh"); // Must match the path used in login
        cookie.setMaxAge(0); // Immediately expire the cookie
        cookie.setSecure(true);
        response.addCookie(cookie);

        return ResponseEntity.noContent().build();
    }

    /**
     * Exception handler for BadCredentialsException.
     *
     * @param e The exception
     * @return 401 Unauthorized
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentialsException(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
