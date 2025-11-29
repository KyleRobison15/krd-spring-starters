package com.krd.starter.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Request DTO for initiating a checkout process.
 */
@Data
public class CheckoutRequest {
    /**
     * The ID of the shopping cart to convert into an order and checkout.
     */
    @NotNull(message = "Cart ID is required.")
    private UUID cartId;
}
