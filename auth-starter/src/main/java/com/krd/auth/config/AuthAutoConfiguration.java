package com.krd.auth.config;

import com.krd.auth.security.JwtAuthenticationFilter;
import com.krd.auth.security.JwtService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for KRD Auth Starter.
 * This class is automatically discovered by Spring Boot and configures all necessary beans.
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.krd.auth")
public class AuthAutoConfiguration {

    /**
     * Configure JWT service if not already provided.
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtService jwtService() {
        return new JwtService();
    }
}
