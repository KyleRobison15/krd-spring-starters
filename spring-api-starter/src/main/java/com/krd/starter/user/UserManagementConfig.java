package com.krd.starter.user;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for user management features.
 * <p>
 * Configure these properties in your application.yml:
 * <pre>
 * app:
 *   user:
 *     hard-delete-after-days: 180
 *     hard-delete-enabled: true
 *     hard-delete-cron: "0 0 2 * * *"
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "app.user")
public class UserManagementConfig {

    /**
     * Number of days to keep soft-deleted users before permanently deleting them.
     * Default: 180 days (6 months)
     */
    private int hardDeleteAfterDays = 180;

    /**
     * Whether to enable the scheduled hard delete task.
     * Set to false to disable automatic hard deletion.
     * Default: true
     */
    private boolean hardDeleteEnabled = true;

    /**
     * Cron expression for when to run the hard delete task.
     * Default: "0 0 2 &#42; &#42; &#42;" (2:00 AM every day)
     * <p>
     * Format: second minute hour day month weekday
     * <p>
     * Examples:
     * <ul>
     *   <li>"0 0 2 &#42; &#42; &#42;" - Every day at 2:00 AM</li>
     *   <li>"0 0 &#42;/6 &#42; &#42; &#42;" - Every 6 hours</li>
     *   <li>"0 0 0 &#42; &#42; MON" - Every Monday at midnight</li>
     * </ul>
     */
    private String hardDeleteCron = "0 0 2 * * *";
}
