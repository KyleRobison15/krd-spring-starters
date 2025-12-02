package com.krd.auth;

import com.krd.auth.config.CorsConfig;
import com.krd.auth.validation.PasswordPolicy;
import com.krd.security.SecurityRules;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Auto-configuration for JWT authentication with Spring Security.
 * <p>
 * This configuration automatically applies when the jwt-auth-starter
 * is added as a dependency. It sets up:
 * <ul>
 *   <li>JWT token generation and validation (JwtService)</li>
 *   <li>JWT authentication filter (JwtAuthenticationFilter)</li>
 *   <li>Password validation with configurable policy (PasswordPolicy)</li>
 *   <li>CORS configuration from application.yaml (CorsConfig)</li>
 *   <li>Spring Security with JWT-based stateless authentication</li>
 *   <li>Modular security rules via SecurityRules interface</li>
 *   <li>Method-level security with @PreAuthorize annotations</li>
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
 * </pre>
 * <p>
 * <strong>Disabling:</strong>
 * To disable JWT authentication, set {@code spring.jwt.enabled=false}
 * <p>
 * <strong>Custom Beans:</strong>
 * You can override any bean by defining your own:
 * <pre>
 * {@code
 * @Configuration
 * public class CustomJwtConfig {
 *     @Bean
 *     public JwtService jwtService(JwtConfig config) {
 *         return new CustomJwtService(config);
 *     }
 *
 *     @Bean
 *     public SecurityFilterChain securityFilterChain(HttpSecurity http,
 *                                                     JwtAuthenticationFilter jwtAuthFilter,
 *                                                     List<SecurityRules> securityRules) throws Exception {
 *         // Custom security configuration
 *         return http.build();
 *     }
 * }
 * }
 * </pre>
 *
 * @see JwtService
 * @see JwtAuthenticationFilter
 * @see JwtConfig
 * @see PasswordPolicy
 * @see CorsConfig
 * @see SecurityRules
 */
@AutoConfiguration
@ConditionalOnProperty(
    name = "spring.jwt.enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties({JwtConfig.class, PasswordPolicy.class, CorsConfig.class})
@ComponentScan(basePackages = "com.krd.auth.validation")
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class JwtAutoConfiguration {

    private final CorsConfig corsConfig;

    /**
     * Creates the JwtService bean for token generation and parsing.
     * <p>
     * Only created if no other JwtService bean is defined (allowing customization).
     *
     * @param config the JWT configuration properties
     * @return the JWT service
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtService jwtService(JwtConfig config) {
        return new JwtService(config);
    }

    /**
     * Creates the JwtAuthenticationFilter bean for request authentication.
     * <p>
     * This filter is automatically registered with Spring Security's filter chain
     * by the {@link #securityFilterChain} bean.
     * <p>
     * Only created if no other JwtAuthenticationFilter bean is defined.
     *
     * @param jwtService the JWT service for token operations
     * @return the JWT authentication filter
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
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
     *   <li>Modular security rules applied from SecurityRules beans</li>
     *   <li>Exception handling returns 401 Unauthorized for authentication failures</li>
     *   <li>Public endpoints: /swagger-ui/**, /v3/api-docs/**, GET /actuator/**</li>
     *   <li>All other endpoints require authentication</li>
     * </ul>
     * <p>
     * <strong>Modular Security Rules:</strong>
     * Add custom security rules by implementing the {@link SecurityRules} interface:
     * <pre>
     * {@code
     * @Component
     * public class MySecurityRules implements SecurityRules {
     *     @Override
     *     public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
     *         registry
     *             .requestMatchers("/auth/**").permitAll()
     *             .requestMatchers(HttpMethod.POST, "/users").permitAll()
     *             .requestMatchers("/admin/**").hasRole("ADMIN");
     *     }
     * }
     * }
     * </pre>
     * <p>
     * <strong>Note:</strong> You can override this bean in your application to
     * customize security configuration completely.
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

                    // Public endpoints (always accessible)
                    auth
                            .requestMatchers("/swagger-ui/**").permitAll()
                            .requestMatchers("/swagger-ui.html/**").permitAll()
                            .requestMatchers("/v3/api-docs/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                            .anyRequest().authenticated();
                })
                .exceptionHandling(exc -> exc
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
