package com.krd.auth.repository;

import com.krd.auth.model.BaseUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * Base repository for user entities.
 * Consuming applications should create their own repository interface extending this.
 *
 * Example usage:
 * @Repository
 * public interface UserRepository extends BaseUserRepository<User> {
 *     // Add custom queries here
 * }
 *
 * @param <T> The concrete user entity type that extends BaseUser
 */
@NoRepositoryBean
public interface BaseUserRepository<T extends BaseUser> extends JpaRepository<T, Long> {

    /**
     * Find a user by email address.
     */
    Optional<T> findByEmail(String email);

    /**
     * Check if a user exists with the given email.
     */
    boolean existsByEmail(String email);
}
