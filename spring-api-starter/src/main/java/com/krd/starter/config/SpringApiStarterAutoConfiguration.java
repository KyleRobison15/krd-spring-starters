package com.krd.starter.config;

import com.krd.security.SecurityRules;
import com.krd.starter.jwt.JwtAuthenticationFilter;
import com.krd.starter.jwt.JwtConfig;
import com.krd.starter.jwt.JwtService;
import com.krd.starter.user.UserManagementConfig;
import com.krd.starter.validation.PasswordPolicy;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Auto-configuration for Spring API Starter.
 * <p>
 * This configuration automatically sets up:
 * <ul>
 *   <li>JWT authentication with JwtService and JwtAuthenticationFilter</li>
 *   <li>Password validation with configurable PasswordPolicy</li>
 *   <li>BCrypt password encoding</li>
 *   <li>Spring Security with JWT-based stateless authentication</li>
 *   <li>CORS configuration from application.yaml</li>
 *   <li>Method-level security with @PreAuthorize annotations</li>
 *   <li>Scheduled tasks for user management (hard delete of soft-deleted users)</li>
 * </ul>
 * <p>
 * <strong>Required Configuration:</strong>
 * <pre>
 * spring:
 *   jwt:
 *     secret: ${JWT_SECRET}
 *     accessTokenExpiration: 900
 *     refreshTokenExpiration: 604800
 * </pre>
 * <p>
 * <strong>Optional Configuration:</strong>
 * <pre>
 * cors:
 *   allowed-origins:
 *     - http://localhost:3000
 *     - http://localhost:5173
 *     - https://myapp.com
 *
 * app:
 *   security:
 *     password:
 *       min-length: 8
 *       max-length: 128
 *       require-uppercase: true
 *       require-lowercase: true
 *       require-digit: true
 *       require-special-char: true
 *   user:
 *     hard-delete-enabled: true
 *     hard-delete-after-days: 180
 *     hard-delete-cron: "0 0 2 * * *"
 * </pre>
 */
@AutoConfiguration
@EnableConfigurationProperties({JwtConfig.class, PasswordPolicy.class, UserManagementConfig.class, CorsConfig.class})
@ComponentScan(basePackages = {
        "com.krd.starter.jwt",
        "com.krd.starter.user",
        "com.krd.starter.validation"
})
@EnableWebSecurity
@EnableMethodSecurity
@EnableScheduling
@AllArgsConstructor
public class SpringApiStarterAutoConfiguration {

    private final JwtService jwtService;
    private final CorsConfig corsConfig;

    /**
     * Creates the BCrypt password encoder bean.
     * <p>
     * BCrypt is a strong, adaptive hashing algorithm recommended for password storage.
     *
     * @return the password encoder
     */
    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates the authentication manager bean.
     * <p>
     * Required for login authentication in AuthService.
     *
     * @param authConfig the authentication configuration
     * @return the authentication manager
     * @throws Exception if authentication manager creation fails
     */
    @Bean
    @ConditionalOnMissingBean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Creates the JWT authentication filter bean.
     *
     * @return the JWT authentication filter
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService);
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) from application.yaml.
     * <p>
     * Configure allowed origins in your application.yaml:
     * <pre>
     * cors:
     *   allowed-origins:
     *     - http://localhost:3000
     *     - http://localhost:5173
     *     - https://myapp.com
     * </pre>
     *
     * @return the CORS configuration source
     */
    @Bean
    @ConditionalOnMissingBean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsConfig.getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configures Spring Security with JWT-based stateless authentication.
     * <p>
     * Security Configuration:
     * <ul>
     *   <li>CSRF disabled (not needed for stateless JWT authentication)</li>
     *   <li>CORS enabled (configured via application.yaml)</li>
     *   <li>Stateless session management (no server-side sessions)</li>
     *   <li>JWT filter added before UsernamePasswordAuthenticationFilter</li>
     *   <li>Public endpoints: /auth/**, POST /users, /swagger-ui/**, /v3/api-docs/**</li>
     *   <li>Modular security rules applied from SecurityRules beans</li>
     *   <li>All other endpoints require authentication</li>
     * </ul>
     * <p>
     * <strong>Note:</strong> You can override this bean in your application to
     * customize security configuration, or add SecurityRules beans for modular configuration.
     * <p>
     * Example override:
     * <pre>
     * {@code
     * @Bean
     * public SecurityFilterChain securityFilterChain(HttpSecurity http,
     *                                                 JwtAuthenticationFilter jwtAuthFilter,
     *                                                 List<SecurityRules> securityRules) throws Exception {
     *     return http
     *         .csrf(AbstractHttpConfigurer::disable)
     *         .cors(cors -> cors.configurationSource(corsConfigurationSource()))
     *         .authorizeHttpRequests(auth -> {
     *             securityRules.forEach(rule -> rule.configure(auth));
     *             auth.requestMatchers("/auth/**").permitAll()
     *                 .requestMatchers(HttpMethod.POST, "/users").permitAll()
     *                 .requestMatchers("/custom/public/**").permitAll()
     *                 .anyRequest().authenticated();
     *         })
     *         .sessionManagement(session ->
     *             session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
     *         )
     *         .exceptionHandling(exc -> exc
     *             .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
     *         .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
     *         .build();
     * }
     * }
     * </pre>
     *
     * @param http the HTTP security configuration
     * @param jwtAuthFilter the JWT authentication filter
     * @param securityRules modular security rules (optional, may be empty list)
     * @return the security filter chain
     * @throws Exception if configuration fails
     */
    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                    JwtAuthenticationFilter jwtAuthFilter,
                                                    List<SecurityRules> securityRules) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> {
                    // Apply modular security rules from SecurityRules beans
                    securityRules.forEach(rule -> rule.configure(auth));

                    // Public endpoints
                    auth
                            .requestMatchers("/swagger-ui/**").permitAll() // Always permit swagger docs
                            .requestMatchers("/swagger-ui.html/**").permitAll() // Always permit swagger docs
                            .requestMatchers("/v3/api-docs/**").permitAll() // Always permit swagger docs
                            .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                            .anyRequest().authenticated();
                })

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // Tell spring to handle authentication and access exceptions
                .exceptionHandling(c -> {
                    c.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
                    c.accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                    });
                })

                .build();
    }
}
