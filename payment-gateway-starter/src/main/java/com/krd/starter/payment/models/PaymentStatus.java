package com.krd.starter.payment.models;

/**
 * Enum representing the status of a payment transaction.
 */
public enum PaymentStatus {
    /**
     * Payment is pending processing
     */
    PENDING,

    /**
     * Payment has been successfully processed
     */
    PAID,

    /**
     * Payment processing failed
     */
    FAILED,

    /**
     * Payment was cancelled by the user or system
     */
    CANCELLED
}
