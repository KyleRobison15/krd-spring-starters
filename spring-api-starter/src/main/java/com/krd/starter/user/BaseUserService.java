package com.krd.starter.user;

import com.krd.starter.user.dto.AddRoleRequest;
import com.krd.starter.user.dto.BaseUserDto;
import com.krd.starter.user.dto.ChangePasswordRequest;
import com.krd.starter.user.dto.RegisterUserRequest;
import com.krd.starter.user.dto.RemoveRoleRequest;
import com.krd.starter.user.dto.UpdateUserRequest;
import com.krd.starter.user.exception.DuplicateUserException;
import com.krd.starter.user.exception.UserNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Abstract base service for user management operations.
 * <p>
 * Provides complete CRUD and role management functionality out of the box.
 * Consumer applications should extend this class with their concrete types:
 * <pre>
 * {@code
 * @Service
 * public class UserService extends BaseUserService<User, UserDto> {
 *     public UserService(UserRepository repository,
 *                        UserMapper mapper,
 *                        PasswordEncoder passwordEncoder,
 *                        RoleChangeLogRepository roleChangeLogRepository) {
 *         super(repository, mapper, passwordEncoder, roleChangeLogRepository);
 *     }
 *     // Add custom methods here if needed
 * }
 * }
 * </pre>
 *
 * @param <T> The concrete user entity type extending BaseUser
 * @param <D> The concrete user DTO type extending BaseUserDto
 */
public abstract class BaseUserService<T extends BaseUser, D extends BaseUserDto> {

    protected final BaseUserRepository<T> repository;
    protected final BaseUserMapper<T, D> mapper;
    protected final PasswordEncoder passwordEncoder;
    protected final RoleChangeLogRepository roleChangeLogRepository;

    protected BaseUserService(BaseUserRepository<T> repository,
                             BaseUserMapper<T, D> mapper,
                             PasswordEncoder passwordEncoder,
                             RoleChangeLogRepository roleChangeLogRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.roleChangeLogRepository = roleChangeLogRepository;
    }

    /**
     * Get all users with optional sorting.
     *
     * @param sort Field to sort by (firstName, lastName, username, or email). Defaults to email.
     * @return List of user DTOs sorted by the specified field
     */
    public Iterable<D> getAllUsers(String sort) {
        if (!Set.of("firstName", "lastName", "username", "email").contains(sort)) {
            sort = "email";
        }

        return repository.findAll(Sort.by(sort))
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    /**
     * Get a user by ID.
     *
     * @param id The user ID
     * @return User DTO
     * @throws UserNotFoundException if user doesn't exist
     */
    public D getUser(Long id) {
        T user = repository.findById(id).orElseThrow(UserNotFoundException::new);
        return mapper.toDto(user);
    }

    /**
     * Register a new user or auto-reactivate a soft-deleted account.
     * <p>
     * Auto-reactivation logic:
     * - If email belongs to a soft-deleted account, it's automatically reactivated
     * - The original account is restored with the new password
     * - All historical data (orders, preferences, etc.) is preserved
     * - Username from the original account is preserved (not updated from request)
     * <p>
     * New user logic:
     * - Validates email uniqueness (for active accounts)
     * - Validates username uniqueness (if provided, for active accounts)
     * - Hashes password before storage
     * - Assigns default USER role
     *
     * @param request Registration request with user details
     * @return Created or reactivated user DTO
     * @throws DuplicateUserException if email or username already exists in active accounts
     */
    public D registerUser(RegisterUserRequest request) {
        // Check if this email belongs to a soft-deleted account
        var deletedEmailWithSuffix = request.getEmail() + "_deleted";
        var deletedUser = repository.findByEmailIncludingDeleted(deletedEmailWithSuffix)
                .filter(user -> user.getDeletedAt() != null)
                .orElse(null);

        if (deletedUser != null) {
            // Auto-reactivate the soft-deleted account
            deletedUser.setDeletedAt(null);
            deletedUser.setEnabled(true);
            deletedUser.setEmail(request.getEmail()); // Restore original email
            deletedUser.setPassword(passwordEncoder.encode(request.getPassword()));

            // Update optional fields from request
            if (request.getFirstName() != null) {
                deletedUser.setFirstName(request.getFirstName());
            }
            if (request.getLastName() != null) {
                deletedUser.setLastName(request.getLastName());
            }
            // Note: Username is not updated - it remains unchanged from the original account

            repository.save(deletedUser);
            return mapper.toDto(deletedUser);
        }

        // No deleted account found - proceed with normal registration

        // Validate email is not in use by active account
        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException();
        }

        // Validate username is not in use (including soft-deleted accounts, since usernames remain locked)
        if (request.getUsername() != null && !request.getUsername().isBlank()
                && repository.existsByUsernameIncludingDeleted(request.getUsername())) {
            throw new DuplicateUserException("A user with this username already exists");
        }

        // Create new user
        T user = mapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of("USER"));
        repository.save(user);

        return mapper.toDto(user);
    }

    /**
     * Update an existing user.
     * <p>
     * Authorization:
     * - Users can update their own profile
     * - Admins can update any profile
     * <p>
     * Validates email and username uniqueness if changed.
     *
     * @param userId  The user ID to update
     * @param request Update request with new values
     * @return Updated user DTO
     * @throws UserNotFoundException    if user doesn't exist
     * @throws AccessDeniedException    if user tries to update someone else's profile
     * @throws DuplicateUserException   if email or username is already in use
     */
    public D updateUser(Long userId, UpdateUserRequest request) {
        var currentUserId = getCurrentUserId();
        var currentUser = repository.findById(currentUserId).orElseThrow();
        var targetUser = repository.findById(userId).orElseThrow(UserNotFoundException::new);

        // Authorization: Users can update themselves, admins can update anyone
        boolean isAdmin = currentUser.getRoles().contains("ADMIN");
        if (!userId.equals(currentUserId) && !isAdmin) {
            throw new AccessDeniedException("You can only update your own profile");
        }

        // Validate email uniqueness if changed
        if (request.getEmail() != null && !request.getEmail().equals(targetUser.getEmail())) {
            if (repository.existsByEmail(request.getEmail())) {
                throw new DuplicateUserException("Email already in use");
            }
        }

        // Validate username uniqueness if changed
        if (request.getUsername() != null && !request.getUsername().equals(targetUser.getUsername())) {
            if (repository.existsByUsername(request.getUsername())) {
                throw new DuplicateUserException("Username already in use");
            }
        }

        mapper.update(request, targetUser);
        repository.save(targetUser);

        return mapper.toDto(targetUser);
    }

    /**
     * Soft delete a user.
     * <p>
     * Security restrictions:
     * - Admins cannot delete themselves (prevents lockout)
     * - Cannot delete the last admin (ensures system access)
     * <p>
     * Soft delete process:
     * 1. Sets deletedAt timestamp
     * 2. Disables the account
     * 3. Appends "_deleted" to email to free up unique constraint
     * 4. Username remains unchanged to preserve user's identity claim
     *
     * @param userId The user ID to delete
     * @throws UserNotFoundException if user doesn't exist
     * @throws AccessDeniedException if admin tries to delete themselves
     * @throws IllegalStateException if trying to delete the last admin
     */
    public void deleteUser(Long userId) {
        var currentUserId = getCurrentUserId();
        var userToDelete = repository.findById(userId).orElseThrow(UserNotFoundException::new);

        // Prevent self-deletion by admin
        if (userId.equals(currentUserId)) {
            throw new AccessDeniedException("Admins cannot delete their own account");
        }

        // Prevent deleting the last admin
        if (userToDelete.getRoles().contains("ADMIN")) {
            long adminCount = repository.countByRolesContaining("ADMIN");
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot delete the last admin user. There must always be at least one admin.");
            }
        }

        // Soft delete: Mark as deleted, disable account, and append "_deleted" to email
        userToDelete.setDeletedAt(LocalDateTime.now());
        userToDelete.setEnabled(false);

        // Append "_deleted" to email to free up unique constraint
        // Username remains unchanged to preserve user's identity claim
        if (!userToDelete.getEmail().endsWith("_deleted")) {
            userToDelete.setEmail(userToDelete.getEmail() + "_deleted");
        }

        repository.save(userToDelete);
    }

    /**
     * Change a user's password.
     * <p>
     * Authorization: Users can only change their own password.
     * <p>
     * Validates:
     * - Current password is correct
     * - New password matches confirmation
     *
     * @param userId  The user ID
     * @param request Change password request
     * @throws UserNotFoundException if user doesn't exist
     * @throws AccessDeniedException if user tries to change someone else's password
     * @throws IllegalArgumentException if current password is wrong or passwords don't match
     */
    public void changePassword(Long userId, ChangePasswordRequest request) {
        var currentUserId = getCurrentUserId();

        // Authorization: Users can only change their own password
        if (!userId.equals(currentUserId)) {
            throw new AccessDeniedException("You can only change your own password");
        }

        var user = repository.findById(userId).orElseThrow(UserNotFoundException::new);

        // Verify current password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password matches confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        // Hash the new password before storing (CRITICAL)
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
    }

    /**
     * Add a role to a user.
     * <p>
     * Logs the role change in the audit log.
     *
     * @param userId  The user ID
     * @param request Add role request
     * @return Updated user DTO
     * @throws UserNotFoundException if user doesn't exist
     */
    public D addRole(Long userId, AddRoleRequest request) {
        var user = repository.findById(userId).orElseThrow(UserNotFoundException::new);

        // Add the role to the user's existing roles
        boolean wasAdded = user.getRoles().add(request.getRole());

        if (wasAdded) {
            repository.save(user);
            logRoleChange(user, request.getRole(), "ADDED");
        }

        return mapper.toDto(user);
    }

    /**
     * Remove a role from a user.
     * <p>
     * Security restrictions:
     * - Admins cannot remove their own ADMIN role (prevents lockout)
     * - Users must have at least one role
     * <p>
     * Logs the role change in the audit log.
     *
     * @param userId  The user ID
     * @param request Remove role request
     * @return Updated user DTO
     * @throws UserNotFoundException if user doesn't exist
     * @throws AccessDeniedException if admin tries to remove their own ADMIN role
     * @throws IllegalStateException if trying to remove the user's last role
     */
    public D removeRole(Long userId, RemoveRoleRequest request) {
        var user = repository.findById(userId).orElseThrow(UserNotFoundException::new);
        var currentUserId = getCurrentUserId();

        // Prevent self-demotion from ADMIN role
        if (userId.equals(currentUserId) && "ADMIN".equals(request.getRole())) {
            throw new AccessDeniedException("Cannot remove ADMIN role from yourself");
        }

        // Ensure user has at least one role
        if (user.getRoles().size() <= 1) {
            throw new IllegalStateException("User must have at least one role");
        }

        // Remove the role
        boolean wasRemoved = user.getRoles().remove(request.getRole());

        if (wasRemoved) {
            repository.save(user);
            logRoleChange(user, request.getRole(), "REMOVED");
        }

        return mapper.toDto(user);
    }

    /**
     * Log a role change to the audit log.
     *
     * @param user   The user whose role was changed
     * @param role   The role that was added or removed
     * @param action "ADDED" or "REMOVED"
     */
    private void logRoleChange(T user, String role, String action) {
        var currentUserId = getCurrentUserId();
        var currentUser = repository.findById(currentUserId).orElse(null);

        var log = RoleChangeLog.builder()
                .userId(user.getId())
                .changedByUserId(currentUserId)
                .role(role)
                .action(action)
                .changedAt(LocalDateTime.now())
                .userEmail(user.getEmail())
                .changedByEmail(currentUser != null ? currentUser.getEmail() : "unknown")
                .build();

        roleChangeLogRepository.save(log);
    }

    /**
     * Get the current authenticated user's ID from the security context.
     *
     * @return Current user ID
     */
    protected Long getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}
