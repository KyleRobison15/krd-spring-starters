package com.krd.starter.user.dto;

import com.krd.starter.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for changing user password.
 * <p>
 * Requires the current password for verification before allowing the change.
 * <p>
 * Example JSON:
 * <pre>
 * {
 *   "oldPassword": "CurrentPass123!",
 *   "newPassword": "NewSecurePass456!",
 *   "confirmPassword": "NewSecurePass456!"
 * }
 * </pre>
 */
@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @ValidPassword
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
}
