package com.krd.starter.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a string value is entirely lowercase.
 * <p>
 * Useful for fields like email addresses that should be stored in lowercase
 * to ensure case-insensitive uniqueness.
 * <p>
 * <strong>Usage:</strong>
 * <pre>
 * &#64;Lowercase(message = "Email must be lowercase")
 * private String email;
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LowercaseValidator.class)
public @interface Lowercase {
    String message() default "Must be lowercase";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
