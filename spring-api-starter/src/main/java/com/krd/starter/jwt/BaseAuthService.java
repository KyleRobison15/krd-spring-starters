package com.krd.starter.jwt;

import com.krd.starter.jwt.dto.LoginRequest;
import com.krd.starter.jwt.dto.LoginResponse;
import com.krd.starter.user.BaseUser;
import com.krd.starter.user.BaseUserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Abstract base service for authentication operations.
 * <p>
 * Provides complete authentication functionality out of the box:
 * <ul>
 *   <li>Login with email/password and JWT token generation</li>
 *   <li>Refresh token handling for generating new access tokens</li>
 *   <li>Get currently authenticated user from security context</li>
 * </ul>
 * <p>
 * Consumer applications should extend this class with their concrete user type:
 * <pre>
 * {@code
 * @Service
 * public class AuthService extends BaseAuthService<User> {
 *     public AuthService(AuthenticationManager authenticationManager,
 *                        UserRepository userRepository,
 *                        JwtService jwtService) {
 *         super(authenticationManager, userRepository, jwtService);
 *     }
 *     // Add custom authentication methods here if needed
 * }
 * }
 * </pre>
 *
 * @param <T> The concrete user entity type extending BaseUser
 */
public abstract class BaseAuthService<T extends BaseUser> {

    protected final AuthenticationManager authenticationManager;
    protected final BaseUserRepository<T> userRepository;
    protected final JwtService jwtService;

    protected BaseAuthService(AuthenticationManager authenticationManager,
                             BaseUserRepository<T> userRepository,
                             JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    /**
     * Get the currently authenticated user from the security context.
     *
     * @return The authenticated user, or null if not found
     */
    public T getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userId = (Long) authentication.getPrincipal();
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * Authenticate a user and generate JWT tokens.
     * <p>
     * This method:
     * 1. Validates the user's credentials using the AuthenticationManager
     * 2. Retrieves the user from the database
     * 3. Generates both access and refresh tokens
     *
     * @param request Login request with email and password
     * @return LoginResponse containing access and refresh tokens
     * @throws org.springframework.security.core.AuthenticationException if credentials are invalid
     */
    public LoginResponse login(LoginRequest request) {
        // Authenticate the user using our Authentication Manager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Get the user from our database so we can generate the JWT for them
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        // Generate access and refresh tokens
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken);
    }

    /**
     * Refresh an access token using a valid refresh token.
     * <p>
     * This method:
     * 1. Parses and validates the refresh token
     * 2. Checks if the token is expired
     * 3. Retrieves the user from the database
     * 4. Generates a new access token
     *
     * @param refreshToken The refresh token string
     * @return A new access token (Jwt)
     * @throws BadCredentialsException if the refresh token is invalid or expired
     */
    public Jwt refreshAccessToken(String refreshToken) {
        var jwt = jwtService.parseToken(refreshToken);
        if (jwt == null || jwt.isExpired()) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        var user = userRepository.findById(jwt.getUserId()).orElseThrow();
        return jwtService.generateAccessToken(user);
    }
}
