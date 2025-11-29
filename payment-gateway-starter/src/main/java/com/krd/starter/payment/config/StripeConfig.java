package com.krd.starter.payment.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for initializing the Stripe API client.
 * <p>
 * Requires the following properties to be configured:
 * <pre>
 * stripe:
 *   secret-key: ${STRIPE_SECRET_KEY}
 *   webhook-secret-key: ${STRIPE_WEBHOOK_SECRET_KEY}
 * </pre>
 */
@Configuration
@ConfigurationProperties(prefix = "stripe")
@Slf4j
public class StripeConfig {

    @Value("${stripe.secretKey}")
    private String secretKey;

    /**
     * Initializes the Stripe API with the configured secret key.
     * Called automatically after bean construction.
     */
    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        log.info("Stripe API initialized");
    }
}
