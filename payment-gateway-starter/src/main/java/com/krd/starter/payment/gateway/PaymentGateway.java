package com.krd.starter.payment.gateway;

import com.krd.starter.payment.models.CheckoutSession;
import com.krd.starter.payment.models.OrderInfo;
import com.krd.starter.payment.models.PaymentResult;
import com.krd.starter.payment.models.WebhookRequest;

import java.util.Optional;

/**
 * Interface defining the contract for payment gateway implementations.
 * <p>
 * Payment gateways are responsible for:
 * <ul>
 *   <li>Creating checkout sessions for customers to complete payment</li>
 *   <li>Processing webhook events from the payment provider</li>
 *   <li>Verifying webhook signatures for security</li>
 * </ul>
 * <p>
 * Implementations exist for specific payment providers (e.g., Stripe, PayPal).
 */
public interface PaymentGateway {

    /**
     * Creates a checkout session for the given order.
     * <p>
     * The checkout session contains a URL where the customer should be redirected
     * to complete their payment with the payment provider.
     *
     * @param order the order to create a checkout session for
     * @return a checkout session with a payment URL
     * @throws PaymentException if checkout session creation fails
     */
    CheckoutSession createCheckoutSession(OrderInfo order);

    /**
     * Parses and processes a webhook request from the payment provider.
     * <p>
     * This method:
     * <ul>
     *   <li>Verifies the webhook signature to ensure authenticity</li>
     *   <li>Deserializes the webhook event</li>
     *   <li>Extracts payment result information if applicable</li>
     * </ul>
     *
     * @param request the webhook request containing headers and payload
     * @return an Optional containing the payment result if the event is payment-related,
     *         or empty if the event is not relevant to payment status
     * @throws PaymentException if webhook signature verification fails or parsing errors occur
     */
    Optional<PaymentResult> parseWebhookRequest(WebhookRequest request);
}
