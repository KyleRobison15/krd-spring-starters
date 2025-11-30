package com.krd.starter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for CORS (Cross-Origin Resource Sharing).
 * <p>
 * Configure CORS in your application.yaml:
 * <pre>
 * cors:
 *   allowed-origins:
 *     - http://localhost:3000
 *     - http://localhost:5173
 *     - https://myapp.com
 * </pre>
 */
@ConfigurationProperties(prefix = "cors")
@Getter
@Setter
public class CorsConfig {

    /**
     * List of allowed origins for CORS requests.
     * <p>
     * Examples:
     * - http://localhost:3000 (React/Next.js dev server)
     * - http://localhost:5173 (Vite dev server)
     * - https://myapp.com (production frontend)
     */
    private List<String> allowedOrigins = List.of();

}
