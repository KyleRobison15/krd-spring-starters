package com.krd.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Base user entity that can be extended by concrete user implementations.
 * Use @MappedSuperclass to allow inheritance without creating a separate table.
 *
 * Example usage in consuming applications:
 * @Entity
 * @Table(name = "users")
 * public class User extends BaseUser {
 *     // Add custom fields here
 *     private String customField;
 * }
 */
@MappedSuperclass
@Data
@NoArgsConstructor
@SuperBuilder
public abstract class BaseUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "user_addresses", joinColumns = @JoinColumn(name = "user_id"))
    private Set<Address> addresses = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Helper method to add a role to the user.
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }

    /**
     * Helper method to add an address to the user.
     */
    public void addAddress(Address address) {
        this.addresses.add(address);
    }
}
