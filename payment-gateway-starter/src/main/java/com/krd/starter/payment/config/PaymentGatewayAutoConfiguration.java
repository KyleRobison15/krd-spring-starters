package com.krd.starter.payment.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for Payment Gateway Starter.
 * <p>
 * This configuration automatically sets up:
 * <ul>
 *   <li>Stripe payment gateway implementation</li>
 *   <li>Stripe API client configuration</li>
 *   <li>Payment security rules (webhook endpoint authentication bypass)</li>
 *   <li>Payment DTOs and models</li>
 * </ul>
 * <p>
 * <strong>Required Configuration:</strong>
 * <pre>
 * stripe:
 *   secret-key: ${STRIPE_SECRET_KEY}
 *   webhook-secret-key: ${STRIPE_WEBHOOK_SECRET_KEY}
 * websiteUrl: ${WEBSITE_URL}
 * </pre>
 * <p>
 * <strong>Example application.yml:</strong>
 * <pre>
 * stripe:
 *   secretKey: ${STRIPE_SECRET_KEY}
 *   webhookSecretKey: ${STRIPE_WEBHOOK_SECRET_KEY}
 *
 * websiteUrl: http://localhost:3000
 * </pre>
 * <p>
 * <strong>Usage:</strong>
 * <pre>
 * {@code
 * @Service
 * public class CheckoutService {
 *     private final PaymentGateway paymentGateway;
 *
 *     public CheckoutService(PaymentGateway paymentGateway) {
 *         this.paymentGateway = paymentGateway;
 *     }
 *
 *     public CheckoutResponse createCheckout(Order order) {
 *         var session = paymentGateway.createCheckoutSession(order);
 *         return new CheckoutResponse(order.getId(), session.getCheckoutUrl());
 *     }
 * }
 * }
 * </pre>
 */
@AutoConfiguration
@ComponentScan(basePackages = {
        "com.krd.starter.payment.gateway",
        "com.krd.starter.payment.config",
        "com.krd.starter.payment.security"
})
public class PaymentGatewayAutoConfiguration {
    // All beans are auto-discovered via component scanning
}
