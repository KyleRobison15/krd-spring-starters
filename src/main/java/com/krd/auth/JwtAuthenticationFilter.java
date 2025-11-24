package com.krd.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter that intercepts HTTP requests to authenticate users via JWT tokens.
 * <p>
 * This filter:
 * <ol>
 *   <li>Extracts the JWT token from the Authorization header</li>
 *   <li>Validates and parses the token</li>
 *   <li>Sets the Spring Security context with user authentication</li>
 * </ol>
 * <p>
 * The filter expects tokens in the format: {@code Authorization: Bearer <token>}
 * <p>
 * If no token is present or the token is invalid, the request proceeds without
 * authentication. Protected endpoints will then return 401/403 errors as configured
 * by Spring Security.
 *
 * @see JwtService
 * @see Jwt
 */
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    /**
     * Processes each HTTP request to extract and validate JWT tokens.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain to continue processing
     * @throws ServletException if an error occurs during filter processing
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        // Extract the Authorization header from the request
        var authHeader = request.getHeader("Authorization");

        // Ensure the Authorization header is properly formed (Bearer token)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token or improperly formatted - proceed without authentication
            // Spring Security will handle authorization for protected endpoints
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the token (remove "Bearer " prefix)
        var token = authHeader.substring(7); // "Bearer ".length() == 7
        var jwt = jwtService.parseToken(token);

        // Validate the token
        if (jwt == null || jwt.isExpired() || !jwt.isEnabled()) {
            // Invalid, expired, or disabled user - proceed without authentication
            filterChain.doFilter(request, response);
            return;
        }

        // At this point, we have a valid token for an enabled user
        // Convert roles to Spring Security authorities
        var authorities = jwt.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        // Create authentication token with user ID as principal and roles as authorities
        var authentication = new UsernamePasswordAuthenticationToken(
                jwt.getUserId(),  // Principal (can be retrieved in controllers with @AuthenticationPrincipal)
                null,             // Credentials (not needed after authentication)
                authorities       // Authorities (roles for authorization)
        );

        // Attach additional request metadata to the authentication
        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        // Store the authentication in Spring Security context
        // This makes the user authenticated for this request
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}
