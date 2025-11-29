package com.krd.starter.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled service for automatically hard deleting soft-deleted users after a configurable period.
 * <p>
 * This service runs on a schedule (default: 2 AM daily) and permanently deletes users
 * that have been soft-deleted for longer than the configured threshold.
 * <p>
 * Configuration:
 * <pre>
 * app:
 *   user:
 *     hard-delete-enabled: true  # Enable/disable the scheduled task
 *     hard-delete-after-days: 180  # Days to keep soft-deleted users
 *     hard-delete-cron: "0 0 2 * * *"  # When to run (2 AM daily)
 * </pre>
 * <p>
 * The scheduler is only enabled if {@code app.user.hard-delete-enabled=true} (default).
 *
 * @param <T> The concrete user entity type extending BaseUser
 */
@Service
@Slf4j
@ConditionalOnProperty(
    prefix = "app.user",
    name = "hard-delete-enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class UserHardDeleteScheduler<T extends BaseUser> {

    private final BaseUserRepository<T> userRepository;
    private final UserManagementConfig config;

    public UserHardDeleteScheduler(BaseUserRepository<T> userRepository,
                                   UserManagementConfig config) {
        this.userRepository = userRepository;
        this.config = config;
    }

    /**
     * Scheduled task to hard delete old soft-deleted users.
     * <p>
     * Runs according to the cron expression defined in {@code app.user.hard-delete-cron}.
     * Default: Every day at 2:00 AM
     * <p>
     * This method:
     * 1. Calculates the threshold date (now - configured days)
     * 2. Finds all users soft-deleted before that date
     * 3. Permanently deletes them from the database
     * 4. Logs the operation for audit purposes
     */
    @Scheduled(cron = "${app.user.hard-delete-cron:0 0 2 * * *}")
    @Transactional
    public void hardDeleteOldSoftDeletedUsers() {
        log.info("Starting scheduled hard delete of old soft-deleted users...");
        log.info("Hard delete threshold: {} days", config.getHardDeleteAfterDays());

        LocalDateTime threshold = LocalDateTime.now()
            .minusDays(config.getHardDeleteAfterDays());

        log.debug("Querying for users soft-deleted before: {}", threshold);

        List<T> usersToDelete = userRepository.findByDeletedAtBefore(threshold);

        if (usersToDelete.isEmpty()) {
            log.info("No users found for hard deletion");
            return;
        }

        log.info("Found {} user(s) to hard delete", usersToDelete.size());

        int deletedCount = 0;
        for (T user : usersToDelete) {
            try {
                log.info("Hard deleting user: id={}, email={}, deletedAt={}",
                    user.getId(), user.getEmail(), user.getDeletedAt());

                userRepository.delete(user);
                deletedCount++;

                log.debug("Successfully hard deleted user: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to hard delete user: id={}, email={}. Error: {}",
                    user.getId(), user.getEmail(), e.getMessage(), e);
                // Continue with next user even if one fails
            }
        }

        log.info("Hard delete completed: {} of {} user(s) successfully deleted",
            deletedCount, usersToDelete.size());
    }

    /**
     * Manually trigger hard delete of a specific user.
     * <p>
     * This method can be called directly (e.g., from an admin endpoint) to immediately
     * hard delete a soft-deleted user without waiting for the scheduled task.
     * <p>
     * Useful for compliance requirements like GDPR "right to be forgotten".
     *
     * @param userId the ID of the user to hard delete
     * @throws IllegalStateException if the user is not soft-deleted
     * @throws IllegalArgumentException if the user does not exist
     */
    @Transactional
    public void hardDeleteUser(Long userId) {
        log.info("Manual hard delete requested for user ID: {}", userId);

        T user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (user.getDeletedAt() == null) {
            throw new IllegalStateException(
                "Cannot hard delete user that is not soft-deleted. User must be soft-deleted first.");
        }

        log.info("Hard deleting user: id={}, email={}, deletedAt={}",
            user.getId(), user.getEmail(), user.getDeletedAt());

        userRepository.delete(user);

        log.info("Successfully hard deleted user: {}", user.getEmail());
    }
}
