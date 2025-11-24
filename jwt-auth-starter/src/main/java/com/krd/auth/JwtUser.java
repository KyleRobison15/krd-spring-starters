package com.krd.auth;

import java.util.Set;

/**
 * Interface that any User entity must implement to work with JWT authentication.
 * <p>
 * This interface defines the contract for user data that will be embedded in JWT tokens.
 * Your User entity should implement this interface to enable JWT authentication.
 * <p>
 * <strong>Login:</strong> Typically users log in with email and password. The username field
 * is primarily for display purposes (e.g., @username in UI, profile URLs).
 * <p>
 * Example implementation:
 * <pre>
 * {@code
 * @Entity
 * @Table(name = "users")
 * public class User implements JwtUser {
 *     @Id
 *     @GeneratedValue(strategy = GenerationType.IDENTITY)
 *     private Long id;
 *
 *     @Column(unique = true, nullable = false)
 *     private String email;
 *
 *     @Column(unique = true, nullable = false)
 *     private String username;
 *
 *     @Column(nullable = false)
 *     private String password; // BCrypt hashed
 *
 *     private String firstName;
 *     private String lastName;
 *
 *     private boolean enabled = true;
 *
 *     @ElementCollection(fetch = FetchType.EAGER)
 *     @Enumerated(EnumType.STRING)
 *     private Set<Role> roles = new HashSet<>();
 *
 *     // Implement JwtUser methods
 *     public Long getId() { return id; }
 *     public String getEmail() { return email; }
 *     public String getUsername() { return username; }
 *     public String getFirstName() { return firstName; }
 *     public String getLastName() { return lastName; }
 *     public boolean isEnabled() { return enabled; }
 *
 *     public Set<String> getRoles() {
 *         return roles.stream()
 *             .map(Role::name)
 *             .collect(Collectors.toSet());
 *     }
 * }
 * }
 * </pre>
 *
 * @see com.krd.auth.JwtService
 * @see com.krd.auth.Jwt
 */
public interface JwtUser {

    /**
     * Returns the unique identifier for this user.
     * <p>
     * This ID is used as the JWT subject (sub claim) and should never change
     * for a given user.
     *
     * @return the user's unique identifier
     */
    Long getId();

    /**
     * Returns the user's email address.
     * <p>
     * The email is typically used for login and communication with the user.
     * Should be unique across all users.
     *
     * @return the user's email address
     */
    String getEmail();

    /**
     * Returns the user's username.
     * <p>
     * The username is primarily for display purposes (e.g., @username in UI,
     * profile URLs, user mentions). While email is used for login, username
     * provides a more user-friendly identifier.
     * <p>
     * Can be the same as email if you don't want a separate username field.
     *
     * @return the user's username
     */
    String getUsername();

    /**
     * Returns the user's first name.
     *
     * @return the user's first name
     */
    String getFirstName();

    /**
     * Returns the user's last name.
     *
     * @return the user's last name
     */
    String getLastName();

    /**
     * Returns the set of roles assigned to this user.
     * <p>
     * Roles are used for authorization and access control. Common roles include:
     * "ADMIN", "USER", "CUSTOMER", "MANAGER", etc.
     * <p>
     * Spring Security will automatically prefix roles with "ROLE_" when checking
     * authorities (e.g., "ADMIN" becomes "ROLE_ADMIN").
     * <p>
     * Example:
     * <pre>
     * {@code
     * // If your User has a Set<Role> where Role is an enum:
     * public Set<String> getRoles() {
     *     return roles.stream()
     *         .map(Role::name)
     *         .collect(Collectors.toSet());
     * }
     *
     * // Or if you store roles as strings directly:
     * public Set<String> getRoles() {
     *     return roles; // Already Set<String>
     * }
     * }
     * </pre>
     *
     * @return a set of role names (e.g., ["ADMIN", "USER"])
     */
    Set<String> getRoles();

    /**
     * Indicates whether the user's account is enabled or disabled.
     * <p>
     * Disabled accounts should not be able to authenticate. This is useful for:
     * <ul>
     *   <li>Suspending user accounts</li>
     *   <li>Soft deletes</li>
     *   <li>Pending account activation</li>
     *   <li>Administrative actions</li>
     * </ul>
     * <p>
     * The JWT service will check this flag and should not issue tokens for
     * disabled accounts.
     *
     * @return true if the account is enabled, false otherwise
     */
    boolean isEnabled();
}
