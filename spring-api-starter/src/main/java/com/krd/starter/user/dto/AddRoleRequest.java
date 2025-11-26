package com.krd.starter.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request DTO for adding a role to a user.
 * <p>
 * Example JSON:
 * <pre>
 * {
 *   "role": "ADMIN"
 * }
 * </pre>
 */
@Data
public class AddRoleRequest {
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(USER|ADMIN)$", message = "Role must be USER or ADMIN")
    private String role;
}
