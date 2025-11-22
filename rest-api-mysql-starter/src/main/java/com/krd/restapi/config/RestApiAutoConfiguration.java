package com.krd.restapi.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for REST API MySQL Starter.
 * This class is automatically discovered by Spring Boot and configures all necessary components.
 *
 * Includes:
 * - Auth functionality from auth-starter
 * - OpenAPI/Swagger configuration
 * - Flyway configuration
 * - Global exception handling
 */
@AutoConfiguration
@ComponentScan(basePackages = {
        "com.krd.restapi",  // This starter's components
        "com.krd.auth"      // Auth starter components
})
public class RestApiAutoConfiguration {
    // Components are auto-discovered via component scanning
}
