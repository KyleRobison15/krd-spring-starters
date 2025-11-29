package com.krd.starter.payment.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a payment checkout session with a URL for the user to complete payment.
 */
@AllArgsConstructor
@Getter
public class CheckoutSession {
    /**
     * The URL where the user should be redirected to complete the payment.
     */
    private String checkoutUrl;
}
