# Spring API Starter

Opinionated Spring Boot starter that provides complete authentication and user management out of the box.

## Features

- ✅ **JWT Authentication** - Dual token system (access + refresh tokens)
- ✅ **User Management** - BaseUser with soft delete support
- ✅ **Password Validation** - Configurable password policy via YAML
- ✅ **CORS Configuration** - Configurable CORS policy via YAML
- ✅ **Role-Based Access Control** - Fine-grained permissions with @PreAuthorize
- ✅ **Audit Logging** - Automatic logging of role changes
- ✅ **Standardized Error Responses** - RFC 7807 compliant ErrorResponse DTO
- ✅ **Scheduled Tasks** - Automatic hard deletion of soft-deleted users
- ✅ **Modular Security** - Works with security-rules-starter for composable security

## Installation

Add to your `build.gradle`:

```gradle
repositories {
    mavenLocal()  // For locally published starters
    mavenCentral()
}

dependencies {
    implementation 'com.krd:spring-api-starter:1.0.0'
}
```

## Quick Start

### 1. Extend BaseUser

```java
@Entity
@Table(name = "users")
public class User extends BaseUser {
    // BaseUser provides: id, firstName, lastName, username, email,
    // password, roles, enabled, deletedAt, createdAt, updatedAt

    // Add your custom fields
    private String phoneNumber;
}
```

### 2. Create Repository

```java
public interface UserRepository extends BaseUserRepository<User> {
    // Inherits all base queries with soft delete support
    // findByEmail, findByUsername, etc.
}
```

### 3. Create DTO

```java
@Data
public class UserDto extends BaseUserDto {
    // BaseUserDto provides: id, firstName, lastName, username,
    // email, roles, createdAt, updatedAt

    // Add your custom fields
    private String phoneNumber;
}
```

### 4. Create Mapper

```java
@Mapper(componentModel = "spring")
public interface UserMapper extends BaseUserMapper<User, UserDto> {
    // MapStruct will generate the implementation
}
```

### 5. Create Service

```java
@Service
public class UserService extends BaseUserService<User, UserDto> {

    public UserService(UserRepository repository,
                      UserMapper mapper,
                      PasswordEncoder passwordEncoder,
                      RoleChangeLogRepository roleChangeLogRepository) {
        super(repository, mapper, passwordEncoder, roleChangeLogRepository);
    }

    @Override
    protected User createNewUser() {
        return new User();
    }
}
```

### 6. Create Controllers

```java
@RestController
@RequestMapping("/users")
@Tag(name = "Users")
public class UserController extends BaseUserController<User, UserDto> {
    public UserController(UserService service) {
        super(service);
    }
}

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController extends BaseAuthController<User, UserDto> {
    public AuthController(JwtConfig jwtConfig,
                         UserMapper userMapper,
                         AuthService authService) {
        super(jwtConfig, userMapper, authService);
    }
}
```

### 7. Create Auth Service

```java
@Service
public class AuthService extends BaseAuthService<User> {

    public AuthService(AuthenticationManager authenticationManager,
                      UserRepository userRepository,
                      JwtService jwtService) {
        super(authenticationManager, userRepository, jwtService);
    }
}
```

### 8. Configure Application

```yaml
spring:
  # JWT Configuration (required)
  jwt:
    secret: ${JWT_SECRET}              # Load from environment
    accessTokenExpiration: 900         # 15 minutes
    refreshTokenExpiration: 604800     # 7 days

# CORS Configuration (required)
cors:
  allowed-origins:
    - http://localhost:3000            # React/Next.js dev server
    - http://localhost:5173            # Vite dev server
    - https://your-production-domain.com

# Application Configuration
app:
  # Password Policy (optional - defaults shown)
  security:
    password:
      min-length: 8
      max-length: 128
      require-uppercase: true
      require-lowercase: true
      require-digit: true
      require-special-char: true

  # User Management (optional - defaults shown)
  user:
    hard-delete-enabled: true          # Enable scheduled hard deletion
    hard-delete-after-days: 180        # Delete after 6 months
    hard-delete-cron: "0 0 2 * * *"    # Run at 2 AM daily
```

### 9. Set Environment Variables

Create a `.env` file:

```properties
JWT_SECRET=your-secret-key-min-256-bits-for-HS256-algorithm
```

## Exception Handling Architecture

This starter provides a standardized error response structure but **does not include exception handlers in base controllers**. Instead, applications should create a centralized `GlobalExceptionHandler` for consistent error handling.

### Why No Exception Handlers in Base Controllers?

Exception handlers defined in base controllers are difficult to override and can lead to inconsistent error responses. The recommended approach is:

1. **ErrorResponse DTO** - Provided by the starter (`com.krd.starter.common.ErrorResponse`)
2. **GlobalExceptionHandler** - Created in your application with `@Order(HIGHEST_PRECEDENCE)`
3. **Centralized Handling** - All exceptions handled in one place for consistency

### ErrorResponse Structure

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/users",
  "errors": [
    {
      "field": "email",
      "message": "must be a well-formed email address",
      "rejectedValue": "invalid-email"
    }
  ]
}
```

### Creating a GlobalExceptionHandler

```java
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> ErrorResponse.FieldError.builder()
                .field(error.getField())
                .message(error.getDefaultMessage())
                .rejectedValue(error.getRejectedValue())
                .build())
            .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message("Validation failed for one or more fields")
            .path(getRequestPath(request))
            .errors(fieldErrors)
            .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error(HttpStatus.NOT_FOUND.getReasonPhrase())
            .message(ex.getMessage())
            .path(getRequestPath(request))
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    // Add handlers for other exceptions...

    private String getRequestPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
```

See the [spring-api-template](https://github.com/your-org/spring-api-template) for a complete example.

## API Endpoints

Once configured, your application automatically provides these endpoints:

### Authentication
- `POST /auth/login` - Authenticate with email/password
- `POST /auth/refresh` - Refresh access token
- `GET /auth/me` - Get current user
- `POST /auth/revoke-refresh-token` - Logout

### User Management
- `POST /users` - Register new user (public)
- `GET /users` - List all users (ADMIN only)
- `GET /users/{id}` - Get user by ID
- `PUT /users/{id}` - Update user (self or ADMIN)
- `DELETE /users/{id}` - Soft delete user (ADMIN only)
- `POST /users/{id}/change-password` - Change password (self only)
- `POST /users/{id}/roles` - Add role to user (ADMIN only)
- `DELETE /users/{id}/roles` - Remove role from user (ADMIN only)
- `GET /users/{id}/roles` - Get user roles (ADMIN only)

## Exceptions

The starter provides these custom exceptions:

- `UserNotFoundException` - Thrown when a user is not found (404)
- `DuplicateUserException` - Thrown when email/username already exists (409)

Your application's `GlobalExceptionHandler` should handle these exceptions.

## Scheduled Tasks

The starter includes automatic hard deletion of soft-deleted users:

- **Runs daily at 2 AM** (configurable via `app.user.hard-delete-cron`)
- **Deletes users** that have been soft-deleted for more than 180 days (configurable)
- **Can be disabled** by setting `app.user.hard-delete-enabled: false`

## Database Schema

The starter expects these tables (create via Flyway migrations):

- `users` - User accounts with soft delete support
- `user_roles` - Many-to-many relationship for user roles
- `role_change_logs` - Audit log for role changes

See the [spring-api-template](https://github.com/your-org/spring-api-template) for example migrations.

## Version

**Current Version:** 1.0.0

## License

MIT
