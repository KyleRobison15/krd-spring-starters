package com.krd.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for the Security Rules Starter.
 *
 * <p>This configuration automatically enables component scanning to discover all {@link SecurityRules}
 * implementations in the application. Each implementation will be automatically registered as a Spring
 * bean and made available for injection into the security configuration.
 *
 * <p><b>Usage in SecurityConfig:</b>
 * <pre>
 * &#64;Configuration
 * &#64;EnableWebSecurity
 * &#64;AllArgsConstructor
 * public class SecurityConfig {
 *
 *     private final List&lt;SecurityRules&gt; securityRules;
 *
 *     &#64;Bean
 *     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
 *         http.authorizeHttpRequests(c -&gt; {
 *             // Apply all discovered security rules
 *             securityRules.forEach(rule -&gt; rule.configure(c));
 *             c.anyRequest().authenticated();
 *         });
 *         return http.build();
 *     }
 * }
 * </pre>
 *
 * <p>This auto-configuration is automatically loaded via Spring Boot's auto-configuration mechanism
 * when the starter is included as a dependency.
 *
 * @see SecurityRules
 * @since 1.0.0
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.krd.security")
public class SecurityRulesAutoConfiguration {
    // No beans to define - just enables component scanning
    // All SecurityRules implementations are discovered automatically via @Component annotation
}
