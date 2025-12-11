# Spring API Starter

Opinionated Spring Boot starter that provides complete authentication and user management out of the box.

## Features

- ✅ **JWT Authentication** - Dual token system (access + refresh tokens)
- ✅ **User Management** - BaseUser with soft delete support
- ✅ **Password Validation** - Configurable password policy via YAML
- ✅ **CORS Configuration** - Configurable CORS policy via YAML
- ✅ **Role-Based Access Control** - Fine-grained permissions with @PreAuthorize
- ✅ **Audit Logging** - Automatic logging of role changes
- ✅ **Automatic Exception Handling** - Includes exception-handling-starter for consistent error responses
- ✅ **Scheduled Tasks** - Automatic hard deletion of soft-deleted users
- ✅ **Modular Security** - Includes security-rules-starter for composable security

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

This starter includes the **exception-handling-starter** which provides automatic exception handling for common errors.

### Automatic Exception Handling

Common exceptions are handled automatically with zero configuration:
- Validation errors (400)
- Malformed JSON (400)
- Authentication failures (401)
- Authorization failures (403)
- Unsupported media types (415)
- Unexpected errors (500)

### ErrorResponse Structure

All errors return a consistent RFC 7807-compliant structure:

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

### Handling Domain-Specific Exceptions

For exceptions specific to your domain (like `UserNotFoundException`), create a handler with higher precedence:

```java
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE) // Higher precedence than starter's global handler
public class MyAppExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error(HttpStatus.NOT_FOUND.getReasonPhrase())
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUserException(
            DuplicateUserException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error(HttpStatus.CONFLICT.getReasonPhrase())
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}
```

**Note:** Import `ErrorResponse` from `com.krd.starter.exception.ErrorResponse`

See the exception-handling-starter README for more details on the two-tier exception handling architecture.

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
