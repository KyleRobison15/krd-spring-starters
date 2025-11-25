# JWT Auth Starter

Spring Boot Starter for JWT Authentication with a dual-token system. Provides automatic JWT token generation, parsing, and validation with minimal configuration.

## ✨ Features

- **Dual-Token System**: Access tokens (short-lived) + Refresh tokens (long-lived)
- **Auto-Configuration**: Automatically configures JWT beans via Spring Boot
- **Interface-Based**: Simple `JwtUser` interface for User entity integration
- **Multi-Role Support**: Multiple roles per user with `Set<String>` roles
- **Account Management**: Built-in enabled/disabled account status
- **Flexible User Model**: Only email required - firstName, lastName, username optional
- **Customizable**: Override default beans and configure token expiration
- **Secure by Default**: HMAC-SHA signing with configurable secret keys

---

## 📦 Installation

### Step 1: Add Dependency

**Gradle:**
```gradle
dependencies {
    implementation 'com.krd:jwt-auth-starter:1.0.0'
}

repositories {
    mavenLocal()
    mavenCentral()
}
```

**Maven:**
```xml
<dependency>
    <groupId>com.krd</groupId>
    <artifactId>jwt-auth-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Step 2: Configure Application Properties

```yaml
spring:
  jwt:
    secret: ${JWT_SECRET}           # Secret key for signing tokens
    accessTokenExpiration: 900      # 15 minutes (in seconds)
    refreshTokenExpiration: 604800  # 7 days (in seconds)
```

**Environment Variable:**
```bash
export JWT_SECRET="your-secure-secret-key-here"
```

---

## 🚀 Quick Start

### 1. Implement JwtUser Interface

Update your User entity to implement the `JwtUser` interface:

```java
import com.krd.auth.JwtUser;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User implements JwtUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    // Optional fields
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "username", unique = true)
    private String username;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    // JwtUser interface methods are automatically provided by Lombok getters
}
```

### 2. Configure Spring Security

Add the JWT authentication filter to your security configuration:

```java
import com.krd.auth.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/register").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### 3. Create Authentication Service

Use the auto-configured `JwtService` to generate tokens:

```java
import com.krd.auth.Jwt;
import com.krd.auth.JwtService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthService(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public LoginResponse login(String email, String password) {
        // Authenticate user (use AuthenticationManager)
        User user = userRepository.findByEmail(email).orElseThrow();

        // Generate tokens
        Jwt accessToken = jwtService.generateAccessToken(user);
        Jwt refreshToken = jwtService.generateRefreshToken(user);

        return new LoginResponse(accessToken.toString(), refreshToken.toString());
    }

    public String refreshAccessToken(String refreshToken) {
        Jwt jwt = jwtService.parseToken(refreshToken);

        if (jwt == null || jwt.isExpired()) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        User user = userRepository.findById(jwt.getUserId()).orElseThrow();
        Jwt newAccessToken = jwtService.generateAccessToken(user);

        return newAccessToken.toString();
    }
}
```

### 4. Access Authenticated User

The JWT filter automatically sets the authenticated user in the SecurityContext:

```java
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public UserDto getCurrentUser() {
        // The principal contains the user ID from the JWT
        Long userId = (Long) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();

        User user = userRepository.findById(userId).orElseThrow();
        return toDto(user);
    }
}
```

---

## 🔧 Configuration

### JWT Properties

| Property | Default | Description |
|----------|---------|-------------|
| `spring.jwt.enabled` | `true` | Enable/disable JWT auto-configuration |
| `spring.jwt.secret` | *required* | Secret key for signing tokens (use environment variable) |
| `spring.jwt.accessTokenExpiration` | *required* | Access token expiration in seconds (recommended: 900 = 15 min) |
| `spring.jwt.refreshTokenExpiration` | *required* | Refresh token expiration in seconds (recommended: 604800 = 7 days) |

### Disable Auto-Configuration

To disable JWT auto-configuration:

```yaml
spring:
  jwt:
    enabled: false
```

### Custom Bean Configuration

Override default beans by defining your own:

```java
@Configuration
public class CustomJwtConfig {

    @Bean
    public JwtService jwtService(JwtConfig config) {
        return new CustomJwtService(config);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
        return new CustomJwtAuthenticationFilter(jwtService);
    }
}
```

---

## 📚 API Reference

### JwtUser Interface

```java
public interface JwtUser {
    Long getId();
    String getEmail();
    String getUsername();
    String getFirstName();
    String getLastName();
    Set<String> getRoles();
    boolean isEnabled();
}
```

**Required Fields:**
- `id` - User identifier
- `email` - User email (used for login)
- `roles` - User roles (can be empty set)
- `enabled` - Account status

**Optional Fields:**
- `username` - Display name (@username)
- `firstName` - User's first name
- `lastName` - User's last name

### JwtService Methods

```java
// Generate tokens
Jwt generateAccessToken(JwtUser user)
Jwt generateRefreshToken(JwtUser user)

// Parse tokens
Jwt parseToken(String token)
```

### Jwt Class Methods

```java
// Token properties
Long getUserId()
String getEmail()
String getUsername()
String getFirstName()
String getLastName()
Set<String> getRoles()
boolean isEnabled()
Date getExpiration()
boolean isExpired()

// Convert to string
String toString()  // Returns the JWT token string
```

---

## 🔒 Security Considerations

1. **Secret Key**: Use a strong, randomly generated secret key (minimum 256 bits)
2. **Environment Variables**: Never commit secrets to version control - use environment variables
3. **HTTPS Only**: Always use HTTPS in production for token transmission
4. **Token Storage**: Store refresh tokens in HttpOnly cookies, access tokens in memory
5. **Token Expiration**: Keep access tokens short-lived (15 minutes recommended)
6. **Account Status**: Check `isEnabled()` before issuing tokens

---

## 🗄️ Database Schema

### Users Table

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    username VARCHAR(255) UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);
```

### User Roles Table (Many-to-Many)

```sql
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

---

## 📖 Complete Example

See the [spring-api-starter](https://github.com/KyleRobison15/spring-api-refresher) project for a complete working example of the JWT Auth Starter integration.

---

## 🐛 Troubleshooting

### "Cannot find symbol: class Jwt"

Make sure you've added the dependency and your IDE has refreshed the Gradle/Maven project.

### "JwtService bean not found"

Ensure `spring.jwt.enabled=true` in your application properties (it's true by default).

### "Invalid token signature"

Check that the `JWT_SECRET` environment variable matches the secret used to generate the token.

### "User is not enabled"

The JWT Auth Starter validates `isEnabled()` when generating tokens. Ensure your user's `enabled` field is `true`.

---

## 📄 License

MIT License - see [LICENSE](../LICENSE) file for details.
