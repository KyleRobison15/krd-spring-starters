package com.krd.starter.jwt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for user login.
 * <p>
 * This class validates login credentials using Bean Validation annotations.
 * <p>
 * Example usage in a controller:
 * <pre>
 * {@code
 * @PostMapping("/login")
 * public JwtResponse login(@Valid @RequestBody LoginRequest request) {
 *     // ...
 * }
 * }
 * </pre>
 */
@Data
public class LoginRequest {

    /**
     * The user's email address (used as login identifier).
     */
    @NotBlank(message = "Email is required.")
    @Email(message = "Must be a valid email.")
    private String email;

    /**
     * The user's password.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 25, message = "Password must be between 6 to 25 characters long")
    private String password;

}
