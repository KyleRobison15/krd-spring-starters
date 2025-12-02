# Spring API Starter

Opinionated Spring Boot starter that provides complete authentication and user management out of the box.

## Features

- ✅ JWT authentication (dual token: access + refresh)
- ✅ User management (BaseUser with soft delete)
- ✅ Password validation (configurable via YAML)
- ✅ Role-based access control
- ✅ Audit logging (role changes)
- ✅ Modular Security Architecture (via `spring-api-starter`).
- ✅ Database migrations (Flyway)

## Installation

Add to your `build.gradle`:

```gradle
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
    // BaseUser provides: id, firstName, lastName, username, email, password, roles, enabled, deletedAt

    // Add custom fields only
    private String phoneNumber;
}
```

### 2. Extend Repository

```java
public interface UserRepository extends BaseUserRepository<User> {
    // Inherits all base queries with soft delete support
}
```

### 3. Extend Service

```java
@Service
public class UserService extends BaseUserService<User, UserRepository, UserDto> {

    @Override
    protected User createNewUser() {
        return new User();
    }

    @Override
    protected UserDto toDto(User user) {
        return mapper.toDto(user);
    }

    @Override
    protected void updateEntityFromRequest(UpdateUserRequest request, User user) {
        mapper.update(request, user);
    }
}
```

### 4. Configure

```yaml
spring:
  jwt:
    secret: ${JWT_SECRET}
    accessTokenExpiration: 900    # 15 minutes
    refreshTokenExpiration: 604800 # 7 days

app:
  security:
    password:
      min-length: 8
      require-uppercase: true
      require-lowercase: true
      require-digit: true
      require-special-char: true
```

### 5. Run

That's it! Your API now has:
- `POST /auth/login` - Authentication
- `POST /auth/refresh` - Refresh tokens
- `POST /users` - User registration
- `GET /users` - List users (admin only)
- `PUT /users/{id}` - Update user
- `DELETE /users/{id}` - Soft delete (admin only)
- `POST /users/{id}/change-password` - Change password
- `POST /users/{id}/roles` - Add role (admin only)
- `DELETE /users/{id}/roles` - Remove role (admin only)

## Documentation

Full documentation coming soon.

## Version

1.0.0

## License

MIT
