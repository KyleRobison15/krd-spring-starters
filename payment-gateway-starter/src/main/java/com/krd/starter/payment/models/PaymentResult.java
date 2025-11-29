package com.krd.starter.payment.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the result of a payment event from a webhook.
 */
@AllArgsConstructor
@Getter
public class PaymentResult {
    /**
     * The ID of the order associated with this payment.
     */
    private Long orderId;

    /**
     * The status of the payment transaction.
     */
    private PaymentStatus paymentStatus;
}
