package com.krd.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Interface for modular security rule configuration.
 *
 * <p>Implementations of this interface define authorization rules for specific features or modules
 * of an application. Each implementation is automatically discovered by Spring and applied to the
 * security filter chain.
 *
 * <p><b>Example implementation:</b>
 * <pre>
 * &#64;Component
 * public class ProductSecurityRules implements SecurityRules {
 *     &#64;Override
 *     public void configure(AuthorizationManagerRequestMatcherRegistry registry) {
 *         registry.requestMatchers(HttpMethod.GET, "/products/**").permitAll()
 *                 .requestMatchers(HttpMethod.POST, "/products/**").hasRole("ADMIN");
 *     }
 * }
 * </pre>
 *
 * <p><b>Benefits:</b>
 * <ul>
 *   <li>Modular organization: Each feature defines its own security rules</li>
 *   <li>Separation of concerns: Security rules stay with the feature they protect</li>
 *   <li>Easy to maintain: Adding/removing features doesn't require changes to central security config</li>
 *   <li>Auto-discovery: Spring automatically finds and applies all implementations</li>
 * </ul>
 *
 * @see SecurityRulesAutoConfiguration
 * @since 1.0.0
 */
public interface SecurityRules {

    /**
     * Configure authorization rules for HTTP requests.
     *
     * @param registry the authorization registry to configure
     */
    void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry);
}
