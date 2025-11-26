package com.krd.starter.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for {@link RoleChangeLog} audit records.
 * <p>
 * Provides methods to query role change history by user or admin.
 */
public interface RoleChangeLogRepository extends JpaRepository<RoleChangeLog, Long> {

    /**
     * Finds all role changes for a specific user, ordered by most recent first.
     *
     * @param userId the user ID
     * @return list of role changes for the user
     */
    List<RoleChangeLog> findByUserIdOrderByChangedAtDesc(Long userId);

    /**
     * Finds all role changes made by a specific admin, ordered by most recent first.
     *
     * @param changedByUserId the admin user ID
     * @return list of role changes made by the admin
     */
    List<RoleChangeLog> findByChangedByUserIdOrderByChangedAtDesc(Long changedByUserId);
}
