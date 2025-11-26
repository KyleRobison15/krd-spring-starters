package com.krd.starter.user;

import com.krd.starter.jwt.JwtUser;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Base user entity providing common user fields and JWT integration.
 * <p>
 * This is a {@link MappedSuperclass} that provides reusable user fields
 * which can be extended by concrete User entities in consumer applications.
 * <p>
 * <strong>Core Fields:</strong>
 * <ul>
 *   <li>id - Primary key</li>
 *   <li>firstName, lastName - User's name</li>
 *   <li>username - Unique display name (e.g., @username)</li>
 *   <li>email - Unique login identifier</li>
 *   <li>password - BCrypt hashed password</li>
 *   <li>roles - Set of role strings for authorization</li>
 *   <li>enabled - Account status (disabled accounts cannot authenticate)</li>
 *   <li>deletedAt - Soft delete timestamp</li>
 * </ul>
 * <p>
 * <strong>Usage:</strong>
 * <pre>
 * {@code
 * @Entity
 * @Table(name = "users")
 * public class User extends BaseUser {
 *     // Add domain-specific fields here
 *     @OneToMany(mappedBy = "user")
 *     private List<Order> orders;
 * }
 * }
 * </pre>
 * <p>
 * <strong>Important:</strong> Use @SuperBuilder instead of @Builder when extending this class.
 *
 * @see com.krd.starter.jwt.JwtUser
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseUser implements JwtUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    /**
     * User roles for authorization.
     * <p>
     * Stored as strings in a separate table (user_roles).
     * Common roles: "USER", "ADMIN", "MANAGER", etc.
     * <p>
     * Spring Security will automatically prefix with "ROLE_" when checking authorities.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    /**
     * Whether the account is enabled.
     * <p>
     * Disabled accounts cannot authenticate.
     * Used for account suspension, soft deletes, or pending activation.
     */
    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    /**
     * Soft delete timestamp.
     * <p>
     * When not null, the user is considered deleted.
     * Soft-deleted users are filtered out by repository queries.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "username = " + username + ", " +
                "email = " + email + ", " +
                "enabled = " + enabled + ")";
    }
}
