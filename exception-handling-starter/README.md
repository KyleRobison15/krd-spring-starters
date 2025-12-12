# Exception Handling Starter

A Spring Boot starter that provides standardized exception handling and error responses for microservices.

## Features

- **Standardized Error Responses**: RFC 7807-compliant error structure for all API errors
- **Auto-Configured Handler**: Automatically handles common exceptions across all microservices
- **Zero Configuration**: Works out of the box with Spring Boot auto-configuration
- **Extensible**: Easy to override or extend with domain-specific exception handlers
- **Consistent**: Ensures uniform error responses across all your microservices

## Installation

### Gradle
```gradle
dependencies {
    implementation 'com.krd:exception-handling-starter:1.0.0'
}
```

### Maven
```xml
<dependency>
    <groupId>com.krd</groupId>
    <artifactId>exception-handling-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## What Gets Handled Automatically

Once you add this starter, the following exceptions are automatically handled:

| Exception | HTTP Status | Description |
|-----------|-------------|-------------|
| `MethodArgumentNotValidException` | 400 Bad Request | Validation errors from `@Valid` or `@Validated` |
| `HttpMessageNotReadableException` | 400 Bad Request | Malformed JSON or invalid request body |
| `IllegalStateException` | 400 Bad Request | Business logic violations |
| `IllegalArgumentException` | 400 or 403 | Invalid arguments or disabled accounts |
| `HttpMediaTypeNotSupportedException` | 415 Unsupported Media Type | Wrong content type |
| `Exception` (catch-all) | 500 Internal Server Error | Unexpected errors |

> **Note:** For Spring Security exception handling (401 Unauthorized, 403 Forbidden), use the **[exception-handling-security-starter](../exception-handling-security-starter)** instead.

## Error Response Structure

All errors return a consistent JSON structure:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/api/chats",
  "errors": [
    {
      "field": "name",
      "message": "Name cannot be blank",
      "rejectedValue": ""
    }
  ]
}
```

**Fields:**
- `timestamp`: When the error occurred
- `status`: HTTP status code (e.g., 400, 404, 500)
- `error`: HTTP status text (e.g., "Bad Request", "Not Found")
- `message`: Human-readable error description
- `path`: The request path that caused the error
- `errors`: (Optional) Field-level validation errors

## Usage

### Basic Usage (Zero Configuration)

Simply add the starter as a dependency. Exception handling is automatically enabled!

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    public User createUser(@Valid @RequestBody CreateUserRequest request) {
        // Validation errors are automatically handled by the starter
        // Returns 400 Bad Request with detailed field errors
        return userService.create(request);
    }
}
```

### Adding Domain-Specific Exception Handlers

Create a `@ControllerAdvice` class with **higher precedence** to handle domain-specific exceptions:

```java
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE) // Higher precedence than starter's handler
public class MyServiceExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex,
            WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }
}
```

### Overriding Common Exception Handlers

If you need to customize handling for a common exception type, create a handler with higher precedence:

```java
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE) // Override starter's handler
public class CustomExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            WebRequest request) {

        // Your custom logic here
        // This will be used instead of the starter's handler
    }
}
```

## Architecture

### Auto-Configuration

The starter uses Spring Boot's auto-configuration mechanism:

1. `ExceptionHandlingAutoConfiguration` enables component scanning
2. `GlobalExceptionHandler` is discovered and registered as a bean
3. Handler is applied with `@Order(Ordered.LOWEST_PRECEDENCE)` to allow overrides
4. Only activates for web applications (`@ConditionalOnWebApplication`)

### Precedence System

The starter's `GlobalExceptionHandler` has **lowest precedence**:
- Your domain-specific handlers (with `@Order(Ordered.HIGHEST_PRECEDENCE)`) execute first
- If no domain handler matches, the starter's handler executes
- This allows selective override while maintaining defaults

## Dependencies

The starter includes:
- `spring-boot-starter-web` (API dependency)
- `spring-boot-starter-validation` (API dependency)
- `spring-boot-autoconfigure` (API dependency)

This starter has **no Spring Security dependency**, making it suitable for microservices without authentication/authorization.

For microservices that **use Spring Security**, add the **[exception-handling-security-starter](../exception-handling-security-starter)** instead, which extends this core starter with Security-specific exception handlers.

## Best Practices

1. **Keep domain handlers focused**: Only handle exceptions specific to your microservice
2. **Use higher precedence**: Always use `@Order(Ordered.HIGHEST_PRECEDENCE)` for domain handlers
3. **Reuse ErrorResponse**: Import `com.krd.starter.exception.ErrorResponse` for consistency
4. **Log appropriately**: The starter logs unexpected errors; add domain-specific logging as needed
5. **Document errors**: Maintain a list of possible errors for API consumers

## Example Projects

### Microservice WITHOUT Spring Security
See the **krd-chat-assistant-svc** project for an example:
- Uses `exception-handling-starter` (core only)
- Domain handler: `ChatExceptionHandler.java`
- Custom exception: `ChatNotFoundException`
- No authentication/authorization

### Microservice WITH Spring Security
See the **chatbot-api** or **spring-api-template** projects for examples:
- Use `exception-handling-security-starter` (core + security)
- Gets 401/403 handling automatically
- JWT authentication enabled

## License

MIT License - see LICENSE file for details
