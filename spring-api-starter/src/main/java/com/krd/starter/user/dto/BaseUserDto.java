package com.krd.starter.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * Base DTO for user data transfer.
 * <p>
 * This provides the common fields that are safe to expose via API responses.
 * Password field is intentionally excluded for security.
 * <p>
 * <strong>Usage:</strong>
 * <pre>
 * {@code
 * @Getter
 * @SuperBuilder
 * public class UserDto extends BaseUserDto {
 *     // Add domain-specific fields
 *     private int orderCount;
 * }
 * }
 * </pre>
 * <p>
 * <strong>Important:</strong> Use @SuperBuilder when extending this class.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
public abstract class BaseUserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Set<String> roles;
    private boolean enabled;
}
