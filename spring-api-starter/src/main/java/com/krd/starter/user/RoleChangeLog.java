package com.krd.starter.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Audit log entity for tracking role changes.
 * <p>
 * Records who changed what role, when, and for which user.
 * This provides an immutable audit trail for security and compliance.
 * <p>
 * <strong>Use Cases:</strong>
 * <ul>
 *   <li>Security audits - track privilege escalations</li>
 *   <li>Compliance - demonstrate access control changes</li>
 *   <li>Troubleshooting - investigate permission issues</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "role_change_logs")
public class RoleChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID of the user whose role was changed.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * ID of the admin who made the change.
     */
    @Column(name = "changed_by_user_id", nullable = false)
    private Long changedByUserId;

    /**
     * The role that was added or removed.
     */
    @Column(name = "role", nullable = false)
    private String role;

    /**
     * The action performed: "ADDED" or "REMOVED".
     */
    @Column(name = "action", nullable = false)
    private String action;

    /**
     * When the change occurred.
     */
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    /**
     * Email of the user (denormalized for easier auditing).
     */
    @Column(name = "user_email")
    private String userEmail;

    /**
     * Email of the admin who made the change (denormalized for easier auditing).
     */
    @Column(name = "changed_by_email")
    private String changedByEmail;
}
