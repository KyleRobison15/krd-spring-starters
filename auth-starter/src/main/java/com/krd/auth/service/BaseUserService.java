package com.krd.auth.service;

import com.krd.auth.dto.JwtResponse;
import com.krd.auth.dto.LoginRequest;
import com.krd.auth.dto.RegisterRequest;
import com.krd.auth.model.BaseUser;
import com.krd.auth.model.Role;
import com.krd.auth.repository.BaseUserRepository;
import com.krd.auth.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

/**
 * Base service for user authentication operations.
 * Consuming applications should extend this and provide their concrete user type.
 *
 * Example usage:
 * @Service
 * public class UserService extends BaseUserService<User> {
 *     public UserService(UserRepository repository, PasswordEncoder passwordEncoder,
 *                       JwtService jwtService, AuthenticationManager authManager,
 *                       UserDetailsService userDetailsService) {
 *         super(repository, passwordEncoder, jwtService, authManager, userDetailsService);
 *     }
 *
 *     @Override
 *     protected User createUserFromRequest(RegisterRequest request) {
 *         User user = new User();
 *         user.setFirstName(request.getFirstName());
 *         user.setLastName(request.getLastName());
 *         user.setEmail(request.getEmail());
 *         user.setPassword(passwordEncoder.encode(request.getPassword()));
 *         user.addRole(Role.ROLE_USER);
 *         // Set custom fields here
 *         return user;
 *     }
 * }
 *
 * @param <T> The concrete user entity type that extends BaseUser
 */
@Service
@RequiredArgsConstructor
public abstract class BaseUserService<T extends BaseUser> {

    protected final BaseUserRepository<T> userRepository;
    protected final PasswordEncoder passwordEncoder;
    protected final JwtService jwtService;
    protected final AuthenticationManager authenticationManager;
    protected final UserDetailsService userDetailsService;

    /**
     * Register a new user.
     * Consuming applications must implement createUserFromRequest to map RegisterRequest to their User type.
     */
    @Transactional
    public JwtResponse register(RegisterRequest request, HttpServletResponse response) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Create user entity
        T user = createUserFromRequest(request);

        // Save user
        T savedUser = userRepository.save(user);

        // Generate tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Set refresh token in httpOnly cookie
        setRefreshTokenCookie(response, refreshToken);

        // Build response
        return buildJwtResponse(savedUser, accessToken);
    }

    /**
     * Login user and generate JWT tokens.
     */
    @Transactional
    public JwtResponse login(LoginRequest request, HttpServletResponse response) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Load user details
        T user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Set refresh token in httpOnly cookie
        setRefreshTokenCookie(response, refreshToken);

        // Build response
        return buildJwtResponse(user, accessToken);
    }

    /**
     * Refresh access token using refresh token from cookie.
     */
    public JwtResponse refreshAccessToken(String refreshToken, HttpServletResponse response) {
        // Extract username from refresh token
        String userEmail = jwtService.extractUsername(refreshToken);

        // Load user
        T user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        // Validate refresh token
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(userDetails);

        // Optionally generate new refresh token (token rotation)
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);
        setRefreshTokenCookie(response, newRefreshToken);

        // Build response
        return buildJwtResponse(user, newAccessToken);
    }

    /**
     * Logout user by clearing refresh token cookie.
     */
    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * Set refresh token in httpOnly cookie.
     */
    protected void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(cookie);
    }

    /**
     * Build JWT response from user entity.
     */
    protected JwtResponse buildJwtResponse(T user, String accessToken) {
        return JwtResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles())
                .build();
    }

    /**
     * Create user entity from registration request.
     * Consuming applications must implement this to map RegisterRequest to their User type.
     */
    protected abstract T createUserFromRequest(RegisterRequest request);
}
