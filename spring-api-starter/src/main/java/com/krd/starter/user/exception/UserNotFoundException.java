package com.krd.starter.user.exception;

/**
 * Exception thrown when a requested user cannot be found.
 * <p>
 * Typically results in a 404 NOT FOUND response.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("User not found");
    }
}
