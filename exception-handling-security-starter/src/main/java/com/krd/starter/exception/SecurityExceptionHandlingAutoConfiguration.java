package com.krd.starter.exception;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for the Exception Handling Security Starter.
 *
 * <p>This configuration automatically enables component scanning to discover the
 * {@link SecurityExceptionHandler} and register it as a Spring bean. The handler will
 * automatically process Spring Security exceptions across all controllers in the application.
 *
 * <p><b>Automatic Behavior:</b>
 * When this starter is included as a dependency in a microservice that uses Spring Security,
 * Security exception handling is automatically enabled. No additional configuration is required.
 *
 * <p><b>Exception Handlers Provided:</b>
 * <ul>
 *     <li>BadCredentialsException → 401 Unauthorized</li>
 *     <li>AccessDeniedException → 403 Forbidden</li>
 *     <li>AuthorizationDeniedException → 403 Forbidden</li>
 * </ul>
 *
 * <p><b>Customization:</b>
 * Microservices can override or extend the Security exception handling by creating their own
 * {@code @ControllerAdvice} classes with higher precedence:
 *
 * <pre>
 * &#64;ControllerAdvice
 * &#64;Order(Ordered.HIGHEST_PRECEDENCE)
 * public class MyCustomSecurityExceptionHandler {
 *
 *     &#64;ExceptionHandler(BadCredentialsException.class)
 *     public ResponseEntity&lt;ErrorResponse&gt; handleBadCredentials(
 *             BadCredentialsException ex, WebRequest request) {
 *         // Custom handling
 *     }
 * }
 * </pre>
 *
 * <p><b>Conditional Activation:</b>
 * This auto-configuration is only activated for web applications. Non-web applications
 * (e.g., batch jobs, CLI tools) will not load this configuration.
 *
 * <p><b>Dependencies:</b>
 * This starter depends on {@code exception-handling-starter} for the core
 * {@link ErrorResponse} class and {@link GlobalExceptionHandler}.
 *
 * @see SecurityExceptionHandler
 * @see ErrorResponse (from exception-handling-starter)
 * @see GlobalExceptionHandler (from exception-handling-starter)
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnWebApplication // Only activate for web applications
@ComponentScan(basePackages = "com.krd.starter.exception")
public class SecurityExceptionHandlingAutoConfiguration {
    // No beans to define - just enables component scanning
    // SecurityExceptionHandler is discovered automatically via @ControllerAdvice annotation
}
