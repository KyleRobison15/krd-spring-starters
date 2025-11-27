package com.krd.starter.user;

import com.krd.starter.user.dto.AddRoleRequest;
import com.krd.starter.user.dto.BaseUserDto;
import com.krd.starter.user.dto.ChangePasswordRequest;
import com.krd.starter.user.dto.RegisterUserRequest;
import com.krd.starter.user.dto.RemoveRoleRequest;
import com.krd.starter.user.dto.UpdateUserRequest;
import com.krd.starter.user.exception.DuplicateUserException;
import com.krd.starter.user.exception.UserNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Set;

/**
 * Abstract base controller for user management endpoints.
 * <p>
 * Provides complete REST API for user operations out of the box:
 * - GET    /users              - List all users (ADMIN only)
 * - GET    /users/{id}         - Get user by ID
 * - POST   /users              - Register new user
 * - PUT    /users/{id}         - Update user
 * - DELETE /users/{id}         - Delete user (ADMIN only)
 * - POST   /users/{id}/change-password - Change password
 * - POST   /users/{id}/roles   - Add role (ADMIN only)
 * - DELETE /users/{id}/roles   - Remove role (ADMIN only)
 * - GET    /users/{id}/roles   - Get user roles (ADMIN only)
 * <p>
 * Consumer applications should extend this class:
 * <pre>
 * {@code
 * @RestController
 * @RequestMapping("/users")
 * @Tag(name = "Users")
 * public class UserController extends BaseUserController<User, UserDto> {
 *     public UserController(UserService service) {
 *         super(service);
 *     }
 *     // Add custom endpoints here if needed
 * }
 * }
 * </pre>
 *
 * @param <T> The concrete user entity type extending BaseUser
 * @param <D> The concrete user DTO type extending BaseUserDto
 */
public abstract class BaseUserController<T extends BaseUser, D extends BaseUserDto> {

    protected final BaseUserService<T, D> service;

    protected BaseUserController(BaseUserService<T, D> service) {
        this.service = service;
    }

    /**
     * Get all users with optional sorting.
     *
     * @param sort Field to sort by (firstName, lastName, username, or email)
     * @return List of user DTOs
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Iterable<D> getAllUsers(@RequestParam(value = "sort", required = false, defaultValue = "") String sort) {
        return service.getAllUsers(sort);
    }

    /**
     * Get a user by ID.
     *
     * @param id User ID
     * @return User DTO
     */
    @GetMapping("/{id}")
    public D getUserById(@PathVariable("id") Long id) {
        return service.getUser(id);
    }

    /**
     * Register a new user.
     *
     * @param request    Registration request
     * @param uriBuilder URI builder for creating location header
     * @return Created user DTO with 201 status and location header
     */
    @PostMapping
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody RegisterUserRequest request,
            UriComponentsBuilder uriBuilder) {

        D userDto = service.registerUser(request);
        var uri = uriBuilder.path("/users/{id}").buildAndExpand(userDto.getId()).toUri();

        return ResponseEntity.created(uri).body(userDto);
    }

    /**
     * Update an existing user.
     * <p>
     * Users can update their own profile, admins can update any profile.
     *
     * @param id      User ID
     * @param request Update request
     * @return Updated user DTO
     */
    @PutMapping("/{id}")
    public D updateUser(@PathVariable("id") Long id, @Valid @RequestBody UpdateUserRequest request) {
        return service.updateUser(id, request);
    }

    /**
     * Soft delete a user.
     * <p>
     * Only accessible by admins.
     * Cannot delete self or the last admin.
     *
     * @param id User ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Change a user's password.
     * <p>
     * Users can only change their own password.
     *
     * @param id      User ID
     * @param request Change password request
     * @return 204 No Content
     */
    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable("id") Long id, @Valid @RequestBody ChangePasswordRequest request) {
        service.changePassword(id, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add a role to a user.
     * <p>
     * Only accessible by admins.
     * All role changes are logged in the audit log.
     *
     * @param id      User ID
     * @param request Add role request
     * @return Updated user DTO
     */
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public D addRole(@PathVariable("id") Long id, @Valid @RequestBody AddRoleRequest request) {
        return service.addRole(id, request);
    }

    /**
     * Remove a role from a user.
     * <p>
     * Only accessible by admins.
     * Cannot remove own ADMIN role or a user's last role.
     * All role changes are logged in the audit log.
     *
     * @param id      User ID
     * @param request Remove role request
     * @return Updated user DTO
     */
    @DeleteMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public D removeRole(@PathVariable("id") Long id, @Valid @RequestBody RemoveRoleRequest request) {
        return service.removeRole(id, request);
    }

    /**
     * Get all roles for a specific user.
     * <p>
     * Only accessible by admins.
     *
     * @param id User ID
     * @return Set of role strings (e.g., ["USER", "ADMIN"])
     */
    @GetMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Set<String>> getUserRoles(@PathVariable("id") Long id) {
        var user = service.getUser(id);
        return ResponseEntity.ok(user.getRoles());
    }

    /**
     * Exception handler for UserNotFoundException.
     *
     * @param e The exception
     * @return 404 Not Found with error message
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    /**
     * Exception handler for IllegalStateException.
     *
     * @param e The exception
     * @return 400 Bad Request with error message
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

    /**
     * Exception handler for DuplicateUserException.
     *
     * @param e The exception
     * @return 400 Bad Request with error message
     */
    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<?> handleDuplicateUserException(DuplicateUserException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
}
