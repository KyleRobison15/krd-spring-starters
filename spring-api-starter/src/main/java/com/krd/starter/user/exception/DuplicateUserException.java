package com.krd.starter.user.exception;

/**
 * Exception thrown when attempting to create a user with an email or username
 * that already exists in the system.
 * <p>
 * Typically results in a 400 BAD REQUEST response.
 */
public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException() {
        super("A user with this email already exists.");
    }

    public DuplicateUserException(String message) {
        super(message);
    }
}
