package com.krd.starter.payment.dto;

import lombok.Data;

/**
 * Response DTO returned after initiating a checkout.
 * Contains the created order ID and the URL to redirect the user for payment.
 */
@Data
public class CheckoutResponse {
    /**
     * The ID of the created order.
     */
    private Long orderId;

    /**
     * The URL where the user should be redirected to complete payment.
     * This URL is provided by the payment gateway (e.g., Stripe Checkout).
     */
    private String stripeCheckoutUrl;

    public CheckoutResponse(Long orderId, String stripeCheckoutUrl) {
        this.orderId = orderId;
        this.stripeCheckoutUrl = stripeCheckoutUrl;
    }
}
