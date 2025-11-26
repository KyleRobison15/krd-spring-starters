-- ============================================================================
-- User Management Tables
-- ============================================================================
-- This migration creates the base tables required for user authentication
-- and authorization using the spring-api-starter.
--
-- Tables created:
-- - users: Core user entity with authentication fields
-- - user_roles: Many-to-many relationship for user roles
-- - role_change_logs: Audit trail for role modifications
-- ============================================================================

-- Create users table
CREATE TABLE users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255)        NULL,
    last_name  VARCHAR(255)        NULL,
    username   VARCHAR(255) UNIQUE NULL,
    email      VARCHAR(255) UNIQUE NOT NULL,
    password   VARCHAR(255)        NOT NULL,
    enabled    BOOLEAN DEFAULT TRUE NOT NULL,
    deleted_at DATETIME            NULL,

    -- Indexes for performance
    INDEX idx_users_email (email),
    INDEX idx_users_username (username),
    INDEX idx_users_deleted_at (deleted_at),
    INDEX idx_users_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create user_roles table (many-to-many relationship)
CREATE TABLE user_roles
(
    user_id BIGINT      NOT NULL,
    role    VARCHAR(50) NOT NULL,

    PRIMARY KEY (user_id, role),

    -- CASCADE: When user is deleted, their roles are deleted too
    -- This aligns with JPA @ElementCollection behavior
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE,

    INDEX idx_user_roles_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create role change audit log table
CREATE TABLE role_change_logs
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT       NULL COMMENT 'Nullable to preserve audit trail if user is hard-deleted',
    changed_by_user_id BIGINT       NULL COMMENT 'Nullable to preserve audit trail if admin is hard-deleted',
    role               VARCHAR(50)  NOT NULL,
    action             VARCHAR(20)  NOT NULL COMMENT 'ADDED or REMOVED',
    changed_at         DATETIME     NOT NULL,
    user_email         VARCHAR(255) NULL COMMENT 'Denormalized for easier auditing',
    changed_by_email   VARCHAR(255) NULL COMMENT 'Denormalized for easier auditing',

    -- SET NULL: Preserves audit trail even if user is hard-deleted
    -- Note: Soft deletes (deletedAt) mean this rarely happens in practice
    CONSTRAINT fk_role_change_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_role_change_by_user
        FOREIGN KEY (changed_by_user_id) REFERENCES users (id)
            ON DELETE SET NULL,

    INDEX idx_role_change_user_id (user_id),
    INDEX idx_role_change_by_user_id (changed_by_user_id),
    INDEX idx_role_change_changed_at (changed_at),
    INDEX idx_role_change_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
