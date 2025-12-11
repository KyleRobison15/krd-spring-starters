package com.krd.starter.exception;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for the Exception Handling Starter.
 *
 * <p>This configuration automatically enables component scanning to discover the
 * {@link GlobalExceptionHandler} and register it as a Spring bean. The handler will
 * automatically process exceptions across all controllers in the application.
 *
 * <p><b>Automatic Behavior:</b>
 * When this starter is included as a dependency, exception handling is automatically enabled.
 * No additional configuration is required in the consuming application.
 *
 * <p><b>Customization:</b>
 * Microservices can override or extend the global exception handling by creating their own
 * {@code @ControllerAdvice} classes with higher precedence:
 *
 * <pre>
 * &#64;ControllerAdvice
 * &#64;Order(Ordered.HIGHEST_PRECEDENCE)
 * public class MyServiceExceptionHandler {
 *
 *     &#64;ExceptionHandler(MyCustomException.class)
 *     public ResponseEntity&lt;ErrorResponse&gt; handleMyException(
 *             MyCustomException ex, WebRequest request) {
 *         // Handle domain-specific exception
 *     }
 * }
 * </pre>
 *
 * <p><b>Conditional Activation:</b>
 * This auto-configuration is only activated for web applications. Non-web applications
 * (e.g., batch jobs, CLI tools) will not load this configuration.
 *
 * <p>This auto-configuration is automatically loaded via Spring Boot's auto-configuration
 * mechanism when the starter is included as a dependency.
 *
 * @see GlobalExceptionHandler
 * @see ErrorResponse
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnWebApplication // Only activate for web applications
@ComponentScan(basePackages = "com.krd.starter.exception")
public class ExceptionHandlingAutoConfiguration {
    // No beans to define - just enables component scanning
    // GlobalExceptionHandler is discovered automatically via @ControllerAdvice annotation
}
