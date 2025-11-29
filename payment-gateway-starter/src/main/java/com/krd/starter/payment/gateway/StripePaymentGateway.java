package com.krd.starter.payment.gateway;

import com.krd.starter.payment.models.*;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Stripe implementation of the PaymentGateway interface.
 * <p>
 * Handles:
 * <ul>
 *   <li>Creating Stripe Checkout sessions</li>
 *   <li>Processing Stripe webhook events</li>
 *   <li>Verifying webhook signatures</li>
 * </ul>
 * <p>
 * Required configuration:
 * <pre>
 * stripe:
 *   secret-key: ${STRIPE_SECRET_KEY}
 *   webhook-secret-key: ${STRIPE_WEBHOOK_SECRET_KEY}
 * websiteUrl: ${WEBSITE_URL}
 * </pre>
 */
@Service
@Slf4j
public class StripePaymentGateway implements PaymentGateway {

    @Value("${websiteUrl}")
    private String websiteUrl;

    @Value("${stripe.webhookSecretKey}")
    private String webhookSecretKey;

    /**
     * Creates a Stripe Checkout session for the given order.
     *
     * @param order the order containing line items to checkout
     * @return checkout session with the Stripe Checkout URL
     * @throws PaymentException if Stripe API call fails
     */
    @Override
    public CheckoutSession createCheckoutSession(OrderInfo order) {
        try {
            log.debug("Creating Stripe checkout session for order ID: {}", order.getOrderId());

            // Build Stripe Session parameters
            var builder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(websiteUrl + "/checkout-success?orderId=" + order.getOrderId())
                    .setCancelUrl(websiteUrl + "/checkout-cancel")
                    .setPaymentIntentData(createPaymentIntent(order));

            // Add each line item to the Stripe session
            order.getLineItems().forEach(item -> {
                var lineItem = createLineItem(item);
                builder.addLineItem(lineItem);
            });

            // Create the Stripe Session
            var session = Session.create(builder.build());
            log.info("Created Stripe checkout session for order {}: {}", order.getOrderId(), session.getId());

            return new CheckoutSession(session.getUrl());
        } catch (StripeException e) {
            log.error("Failed to create Stripe checkout session for order {}: {}",
                order.getOrderId(), e.getMessage(), e);
            throw new PaymentException("Failed to create checkout session", e);
        }
    }

    /**
     * Creates PaymentIntent metadata with the order ID.
     *
     * @param order the order
     * @return PaymentIntent parameters with order ID in metadata
     */
    private SessionCreateParams.PaymentIntentData createPaymentIntent(OrderInfo order) {
        return SessionCreateParams.PaymentIntentData.builder()
                .putMetadata("order_id", order.getOrderId().toString())
                .build();
    }

    /**
     * Parses a webhook request from Stripe and extracts payment result if applicable.
     *
     * @param request the webhook request with headers and payload
     * @return Optional containing PaymentResult if event is payment-related, empty otherwise
     * @throws PaymentException if signature verification fails or deserialization errors occur
     */
    @Override
    public Optional<PaymentResult> parseWebhookRequest(WebhookRequest request) {
        try {
            var payload = request.getPayload();
            var signature = request.getHeaders().get("stripe-signature");

            // Verify and construct the event from Stripe
            var event = Webhook.constructEvent(payload, signature, webhookSecretKey);
            log.debug("Received Stripe webhook event: {}", event.getType());

            // Process the event based on type
            return switch (event.getType()) {
                case "payment_intent.succeeded" -> {
                    log.info("Payment succeeded for order: {}", extractOrderId(event));
                    yield Optional.of(new PaymentResult(extractOrderId(event), PaymentStatus.PAID));
                }
                case "payment_intent.payment_failed" -> {
                    log.warn("Payment failed for order: {}", extractOrderId(event));
                    yield Optional.of(new PaymentResult(extractOrderId(event), PaymentStatus.FAILED));
                }
                default -> {
                    log.debug("Unhandled webhook event type: {}", event.getType());
                    yield Optional.empty();
                }
            };

        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe webhook signature: {}", e.getMessage());
            throw new PaymentException("Invalid webhook signature", e);
        }
    }

    /**
     * Extracts the order ID from a Stripe webhook event's metadata.
     *
     * @param event the Stripe event
     * @return the order ID
     * @throws PaymentException if event cannot be deserialized or order ID is missing
     */
    private Long extractOrderId(Event event) {
        var stripeObject = event.getDataObjectDeserializer().getObject().orElseThrow(
                () -> new PaymentException("Could not deserialize Stripe event. Check SDK and API versions.")
        );

        var paymentIntent = (PaymentIntent) stripeObject;
        var orderIdStr = paymentIntent.getMetadata().get("order_id");

        if (orderIdStr == null) {
            throw new PaymentException("Order ID not found in payment intent metadata");
        }

        return Long.valueOf(orderIdStr);
    }

    /**
     * Creates a Stripe line item from an order line item.
     *
     * @param item the order line item
     * @return Stripe line item parameters
     */
    private SessionCreateParams.LineItem createLineItem(OrderInfo.LineItem item) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity(Long.valueOf(item.getQuantity()))
                .setPriceData(createPriceData(item))
                .build();
    }

    /**
     * Creates Stripe price data from an order line item.
     *
     * @param item the order line item
     * @return Stripe price data parameters
     */
    private SessionCreateParams.LineItem.PriceData createPriceData(OrderInfo.LineItem item) {
        return SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("usd")
                // Convert dollars to cents (Stripe requires smallest unit of currency)
                .setUnitAmountDecimal(item.getUnitPrice().multiply(BigDecimal.valueOf(100)))
                .setProductData(createProductData(item))
                .build();
    }

    /**
     * Creates Stripe product data from an order line item.
     *
     * @param item the order line item
     * @return Stripe product data parameters
     */
    private SessionCreateParams.LineItem.PriceData.ProductData createProductData(OrderInfo.LineItem item) {
        return SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(item.getName())
                .build();
    }
}
