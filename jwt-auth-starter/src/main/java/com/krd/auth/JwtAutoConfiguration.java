package com.krd.auth;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for JWT authentication.
 * <p>
 * This configuration is automatically applied when the jwt-auth-starter
 * is added as a dependency. It sets up all necessary beans for JWT authentication.
 * <p>
 * <strong>Configuration:</strong>
 * <pre>
 * spring:
 *   jwt:
 *     enabled: true  # Optional, defaults to true
 *     secret: ${JWT_SECRET}
 *     accessTokenExpiration: 900
 *     refreshTokenExpiration: 604800
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
 * }
 * }
 * </pre>
 *
 * @see JwtService
 * @see JwtAuthenticationFilter
 * @see JwtConfig
 */
@AutoConfiguration
@ConditionalOnProperty(
    name = "spring.jwt.enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(JwtConfig.class)
public class JwtAutoConfiguration {

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
     * This filter must be registered with Spring Security's filter chain
     * (typically done in your SecurityConfig).
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
}
