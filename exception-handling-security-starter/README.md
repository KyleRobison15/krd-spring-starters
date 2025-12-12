# Exception Handling Security Starter

A Spring Boot starter that extends [exception-handling-starter](../exception-handling-starter) with Spring Security exception handlers for microservices using authentication/authorization.

## Features

- **Extends Core Starter**: Automatically includes exception-handling-starter (core exception handling)
- **Security Exception Handlers**: Handles BadCredentialsException (401) and AccessDeniedException (403)
- **Auto-Configured**: Works out of the box with Spring Boot auto-configuration
- **Zero Configuration**: Simply add as a dependency and get Security exception handling
- **Consistent Responses**: Uses the same ErrorResponse structure as core starter

## Installation

### Gradle
```gradle
dependencies {
    implementation 'com.krd:exception-handling-security-starter:1.0.0'
}
```

### Maven
```xml
<dependency>
    <groupId>com.krd</groupId>
    <artifactId>exception-handling-security-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

> **Note:** This starter transitively includes `exception-handling-starter`, so you don't need to add both.

## What Gets Handled Automatically

Once you add this starter, the following **Spring Security exceptions** are automatically handled (in addition to all core exceptions):

| Exception | HTTP Status | Description |
|-----------|-------------|-------------|
| `BadCredentialsException` | 401 Unauthorized | Failed login attempts (invalid email/password) |
| `AccessDeniedException` | 403 Forbidden | User lacks required permissions (Spring Security 5.x) |
| `AuthorizationDeniedException` | 403 Forbidden | User lacks required permissions (Spring Security 6.x) |

### Core Exceptions (Inherited from exception-handling-starter)

This starter also handles all core exceptions:

| Exception | HTTP Status | Description |
|-----------|-------------|-------------|
| `MethodArgumentNotValidException` | 400 Bad Request | Validation errors from `@Valid` or `@Validated` |
| `HttpMessageNotReadableException` | 400 Bad Request | Malformed JSON or invalid request body |
| `IllegalStateException` | 400 Bad Request | Business logic violations |
| `IllegalArgumentException` | 400 or 403 | Invalid arguments or disabled accounts |
| `HttpMediaTypeNotSupportedException` | 415 Unsupported Media Type | Wrong content type |
| `Exception` (catch-all) | 500 Internal Server Error | Unexpected errors |

## Error Response Structure

All errors return a consistent JSON structure (same as core starter):

### Example: Invalid Login (401)
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/auth/login"
}
```

### Example: Access Denied (403)
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to access this resource",
  "path": "/admin/users"
}
```

### Example: Validation Error (400)
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/auth/register",
  "errors": [
    {
      "field": "email",
      "message": "Email must be valid",
      "rejectedValue": "invalid-email"
    }
  ]
}
```

## Usage

### Basic Usage (Zero Configuration)

Simply add the starter as a dependency. Security exception handling is automatically enabled!

```java
@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        // BadCredentialsException automatically returns 401 with proper message
        return authService.login(request);
    }
}

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @GetMapping("/users")
    public List<User> getAllUsers() {
        // AccessDeniedException automatically returns 403 with proper message
        return userService.findAll();
    }
}
```

### Customizing Security Exception Handlers

If you need custom handling for Security exceptions, create a `@ControllerAdvice` class with **higher precedence**:

```java
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE) // Higher precedence than starter's handler
public class CustomSecurityExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            WebRequest request) {

        // Custom logic - maybe track failed login attempts
        loginAttemptService.recordFailedAttempt(request);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Login failed. Account may be locked after 5 attempts.")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }
}
```

## Architecture

### How It Works

1. **Extends Core Starter**: This starter depends on `exception-handling-starter`
2. **Adds Security Handlers**: Provides `SecurityExceptionHandler` for Spring Security exceptions
3. **No Code Duplication**: Reuses `ErrorResponse` from core starter
4. **Auto-Configuration**: `SecurityExceptionHandlingAutoConfiguration` registers handlers automatically
5. **Web Application Only**: Only activates for web applications (`@ConditionalOnWebApplication`)

### Handler Precedence

Both `GlobalExceptionHandler` (core) and `SecurityExceptionHandler` (security) use `@Order(Ordered.LOWEST_PRECEDENCE)`:
- Your domain-specific handlers with `@Order(Ordered.HIGHEST_PRECEDENCE)` execute first
- If no domain handler matches, starter handlers execute
- This allows selective override while maintaining defaults

### Transitive Dependencies

When you add `exception-handling-security-starter`, you automatically get:

```
exception-handling-security-starter
├── exception-handling-starter (core)
│   ├── spring-boot-starter-web
│   ├── spring-boot-starter-validation
│   └── spring-boot-autoconfigure
└── spring-boot-starter-security
```

## Dependencies

The starter includes:
- `exception-handling-starter:1.0.0` (API dependency, transitively included)
- `spring-boot-starter-security` (API dependency)
- `spring-boot-autoconfigure` (API dependency)

## When to Use This Starter

### ✅ Use exception-handling-security-starter when:
- Your microservice uses Spring Security
- You need JWT authentication
- You have user management with roles/permissions
- You need 401/403 error handling

### ❌ Use exception-handling-starter (core) when:
- Your microservice has no authentication
- You're building an anonymous API
- You don't need Spring Security

## Best Practices

1. **One Starter Only**: Use either `exception-handling-starter` OR `exception-handling-security-starter`, not both
2. **Custom Handlers**: Use `@Order(Ordered.HIGHEST_PRECEDENCE)` for domain-specific overrides
3. **Reuse ErrorResponse**: Import `com.krd.starter.exception.ErrorResponse` for consistency
4. **Document Security Errors**: Clearly document which endpoints return 401 vs 403
5. **Test Security Scenarios**: Write integration tests for authentication and authorization failures

## Example Projects

### Projects Using This Starter
- **spring-api-template**: Full-featured API template with JWT auth (via spring-api-starter)
- **chatbot-api**: AI chatbot with user management (via spring-api-starter)

Both projects use `spring-api-starter`, which transitively includes this security starter.

## Comparison with Core Starter

| Feature | exception-handling-starter | exception-handling-security-starter |
|---------|---------------------------|-------------------------------------|
| Validation errors (400) | ✅ | ✅ (inherited) |
| Malformed JSON (400) | ✅ | ✅ (inherited) |
| Media type errors (415) | ✅ | ✅ (inherited) |
| Unexpected errors (500) | ✅ | ✅ (inherited) |
| Bad credentials (401) | ❌ | ✅ |
| Access denied (403) | ❌ | ✅ |
| Spring Security dependency | ❌ No | ✅ Yes |
| Best for | Anonymous APIs | Secured APIs |

## License

MIT License - see LICENSE file for details
