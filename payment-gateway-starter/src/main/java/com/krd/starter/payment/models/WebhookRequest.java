package com.krd.starter.payment.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * Represents an incoming webhook request from a payment provider.
 * Contains the headers and payload needed for signature verification and event processing.
 */
@AllArgsConstructor
@Getter
public class WebhookRequest {
    /**
     * HTTP headers from the webhook request, used for signature verification.
     */
    private Map<String, String> headers;

    /**
     * The raw webhook payload as a string.
     */
    private String payload;
}
