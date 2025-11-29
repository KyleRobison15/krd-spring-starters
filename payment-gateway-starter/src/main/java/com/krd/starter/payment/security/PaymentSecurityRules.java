package com.krd.starter.payment.security;

import com.krd.security.SecurityRules;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

/**
 * Security rules for payment-related endpoints.
 * <p>
 * This component automatically configures Spring Security to:
 * <ul>
 *   <li>Allow unauthenticated POST requests to /checkout/webhook (for payment provider callbacks)</li>
 * </ul>
 * <p>
 * The webhook endpoint must be public because payment providers like Stripe
 * send server-to-server requests without user authentication.
 * Security is ensured through webhook signature verification instead.
 */
@Component
public class PaymentSecurityRules implements SecurityRules {

    /**
     * Configures security rules for payment endpoints.
     *
     * @param registry the Spring Security authorization registry
     */
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        // Allow payment provider webhooks without authentication
        // (security is handled via signature verification in the webhook handler)
        registry.requestMatchers(HttpMethod.POST, "/checkout/webhook").permitAll();
    }
}
