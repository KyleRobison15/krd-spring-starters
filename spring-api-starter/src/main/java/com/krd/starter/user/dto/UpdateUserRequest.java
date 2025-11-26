package com.krd.starter.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

/**
 * Request DTO for updating user profile information.
 * <p>
 * All fields are optional - only provided fields will be updated.
 * <p>
 * Example JSON:
 * <pre>
 * {
 *   "firstName": "Jane",
 *   "email": "newemail@example.com"
 * }
 * </pre>
 */
@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String username;

    @Email(message = "Email must be valid")
    private String email;
}
