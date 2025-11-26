package com.krd.starter.user;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Base repository for user entities with built-in soft delete support.
 * <p>
 * This repository automatically filters out soft-deleted users (deletedAt IS NOT NULL)
 * from all queries.
 * <p>
 * The generic type parameter T must extend {@link BaseUser}.
 * <p>
 * <strong>Usage:</strong>
 * <pre>
 * {@code
 * public interface UserRepository extends BaseUserRepository<User> {
 *     // Add custom queries here
 * }
 * }
 * </pre>
 * <p>
 * <strong>Important:</strong> This is marked with @NoRepositoryBean to prevent
 * Spring Data from creating a repository bean for this interface directly.
 *
 * @param <T> the user entity type that extends BaseUser
 */
@NoRepositoryBean
public interface BaseUserRepository<T extends BaseUser> extends JpaRepository<T, Long> {

    /**
     * Finds a user by ID, excluding soft-deleted users.
     *
     * @param id the user ID
     * @return an Optional containing the user if found and not deleted
     */
    @Query("SELECT u FROM #{#entityName} u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<T> findById(@Param("id") Long id);

    /**
     * Finds all non-deleted users.
     *
     * @return list of all active users
     */
    @Query("SELECT u FROM #{#entityName} u WHERE u.deletedAt IS NULL")
    List<T> findAll();

    /**
     * Finds all non-deleted users with sorting.
     *
     * @param sort the sort specification
     * @return sorted list of all active users
     */
    @Query("SELECT u FROM #{#entityName} u WHERE u.deletedAt IS NULL")
    List<T> findAll(Sort sort);

    /**
     * Finds a user by email, excluding soft-deleted users.
     *
     * @param email the user's email address
     * @return an Optional containing the user if found and not deleted
     */
    @Query("SELECT u FROM #{#entityName} u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<T> findByEmail(@Param("email") String email);

    /**
     * Checks if a user exists with the given email (excluding soft-deleted users).
     *
     * @param email the email to check
     * @return true if a non-deleted user exists with this email
     */
    @Query("SELECT COUNT(u) > 0 FROM #{#entityName} u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsByEmail(@Param("email") String email);

    /**
     * Checks if a user exists with the given username (excluding soft-deleted users).
     *
     * @param username the username to check
     * @return true if a non-deleted user exists with this username
     */
    @Query("SELECT COUNT(u) > 0 FROM #{#entityName} u WHERE u.username = :username AND u.deletedAt IS NULL")
    boolean existsByUsername(@Param("username") String username);

    /**
     * Counts users with a specific role (excluding soft-deleted users).
     *
     * @param role the role to count
     * @return number of active users with the specified role
     */
    @Query("SELECT COUNT(u) FROM #{#entityName} u JOIN u.roles r WHERE r = :role AND u.deletedAt IS NULL")
    long countByRolesContaining(@Param("role") String role);
}
