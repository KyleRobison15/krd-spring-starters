package com.krd.auth.controller;

import com.krd.auth.dto.JwtResponse;
import com.krd.auth.dto.LoginRequest;
import com.krd.auth.dto.RegisterRequest;
import com.krd.auth.model.BaseUser;
import com.krd.auth.service.BaseUserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * Base authentication controller providing standard auth endpoints.
 * Consuming applications can extend this to add custom endpoints or override behavior.
 *
 * Example usage:
 * @RestController
 * @RequestMapping("/api/auth")
 * public class AuthController extends BaseAuthController<User> {
 *     public AuthController(UserService userService) {
 *         super(userService);
 *     }
 *
 *     // Add custom endpoints here
 * }
 *
 * @param <T> The concrete user entity type that extends BaseUser
 */
@RestController
@RequiredArgsConstructor
public abstract class BaseAuthController<T extends BaseUser> {

    protected final BaseUserService<T> userService;

    /**
     * Register a new user.
     * POST /register
     */
    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        JwtResponse jwtResponse = userService.register(request, response);
        return ResponseEntity.ok(jwtResponse);
    }

    /**
     * Login user.
     * POST /login
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        JwtResponse jwtResponse = userService.login(request, response);
        return ResponseEntity.ok(jwtResponse);
    }

    /**
     * Refresh access token using refresh token from cookie.
     * POST /refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // Extract refresh token from cookie
        String refreshToken = getRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }

        JwtResponse jwtResponse = userService.refreshAccessToken(refreshToken, response);
        return ResponseEntity.ok(jwtResponse);
    }

    /**
     * Logout user by clearing refresh token cookie.
     * POST /logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        userService.logout(response);
        return ResponseEntity.ok().build();
    }

    /**
     * Extract refresh token from cookie.
     */
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
