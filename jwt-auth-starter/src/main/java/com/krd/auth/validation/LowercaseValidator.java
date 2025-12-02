package com.krd.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for {@link Lowercase} annotation.
 * <p>
 * Checks that a string value contains only lowercase characters.
 * Null values are considered valid (use @NotNull separately if needed).
 */
public class LowercaseValidator implements ConstraintValidator<Lowercase, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.equals(value.toLowerCase());
    }
}
