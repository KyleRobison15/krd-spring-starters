package com.krd.starter.payment.gateway;

import lombok.NoArgsConstructor;

/**
 * Exception thrown when payment processing encounters an error.
 */
@NoArgsConstructor
public class PaymentException extends RuntimeException {
    /**
     * Constructs a new payment exception with the specified detail message.
     *
     * @param message the detail message
     */
    public PaymentException(String message) {
        super(message);
    }

    /**
     * Constructs a new payment exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
