# JWT Auth Starter

Spring Boot Starter for JWT Authentication with Spring Security. Provides complete security configuration including JWT token generation, password validation, CORS, modular security rules, and automatic SecurityFilterChain setup.

## ✨ Features

- **Dual-Token System**: Access tokens (short-lived) + Refresh tokens (long-lived)
- **Complete Security Configuration**: Auto-configured SecurityFilterChain with JWT filter integration
- **Password Validation**: Configurable password policy with detailed validation messages
- **CORS Configuration**: Easy CORS setup via application.yaml
- **Modular Security Rules**: Extensible security configuration using SecurityRules interface
- **Method Security**: @PreAuthorize annotations enabled out of the box
- **Interface-Based**: Simple `JwtUser` interface for User entity integration
- **Multi-Role Support**: Multiple roles per user with `Set<String>` roles
- **Account Management**: Built-in enabled/disabled account status
- **Flexible User Model**: Only email required - firstName, lastName, username optional
- **Customizable**: Override default beans and configure all aspects
- **Secure by Default**: Stateless sessions, CSRF disabled, proper exception handling

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

**Note:** The jwt-auth-starter automatically includes the security-rules-starter for modular security configuration.

### Step 2: Configure Application Properties

**Required Configuration:**
```yaml
spring:
  jwt:
    secret: ${JWT_SECRET}           # Secret key for signing tokens
    accessTokenExpiration: 900      # 15 minutes (in seconds)
    refreshTokenExpiration: 604800  # 7 days (in seconds)
```

**Optional Configuration:**
```yaml
# CORS Configuration
cors:
  allowed-origins:
    - http://localhost:3000       # React/Next.js dev server
    - http://localhost:5173       # Vite dev server
    - https://yourdomain.com      # Production frontend

# Password Policy
app:
  security:
    password:
      min-length: 8
      max-length: 128
      require-uppercase: true
      require-lowercase: true
      require-digit: true
      require-special-char: true
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

### 2. Add Modular Security Rules

The starter automatically configures Spring Security. To customize which endpoints are public or require authentication, create SecurityRules components:

```java
import com.krd.security.SecurityRules;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class AuthSecurityRules implements SecurityRules {

    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry
            .requestMatchers("/auth/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/users").permitAll();
    }
}

@Component
public class AdminSecurityRules implements SecurityRules {

    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry
            .requestMatchers("/admin/**").hasRole("ADMIN");
    }
}
```

**Default Public Endpoints** (no configuration needed):
- `/swagger-ui/**` - Swagger UI
- `/v3/api-docs/**` - OpenAPI documentation
- `GET /actuator/**` - Actuator endpoints

**Note:** If you don't create any SecurityRules, all other endpoints will require authentication by default.

### 3. Create Authentication Service

Use the auto-configured `JwtService` to generate tokens:

```java
import com.krd.auth.Jwt;
import com.krd.auth.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    public AuthService(JwtService jwtService,
                      UserRepository userRepository,
                      AuthenticationManager authenticationManager) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
    }

    public LoginResponse login(String email, String password) {
        // Authenticate user
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
        );

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

### 4. Use Password Validation

Add the `@ValidPassword` annotation to validate passwords:

```java
import com.krd.auth.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RegisterUserRequest {

    @NotBlank
    @Email
    private String email;

    @ValidPassword  // Validates password against configured policy
    private String password;

    private String firstName;
    private String lastName;
    private String username;
}
```

**Password Validation Errors** provide specific feedback:
- "Password must be at least 8 characters long"
- "Password must contain at least one uppercase letter"
- "Password must contain at least one lowercase letter"
- "Password must contain at least one number"
- "Password must contain at least one special character (@$!%*?&#^()-_=+[]{}|;:,.<>)"

### 5. Access Authenticated User

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

### CORS Properties

| Property | Default | Description |
|----------|---------|-------------|
| `cors.allowed-origins` | `[]` | List of allowed origins for CORS requests |

**Example:**
```yaml
cors:
  allowed-origins:
    - http://localhost:3000
    - https://yourdomain.com
```

### Password Policy Properties

| Property | Default | Description |
|----------|---------|-------------|
| `app.security.password.min-length` | `8` | Minimum password length |
| `app.security.password.max-length` | `128` | Maximum password length |
| `app.security.password.require-uppercase` | `true` | Require at least one uppercase letter |
| `app.security.password.require-lowercase` | `true` | Require at least one lowercase letter |
| `app.security.password.require-digit` | `true` | Require at least one digit |
| `app.security.password.require-special-char` | `true` | Require at least one special character |

### Disable Auto-Configuration

To disable JWT auto-configuration:

```yaml
spring:
  jwt:
    enabled: false
```

### Custom SecurityFilterChain

Override the default SecurityFilterChain by defining your own:

```java
import com.krd.auth.JwtAuthenticationFilter;
import com.krd.security.SecurityRules;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
public class CustomSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                    JwtAuthenticationFilter jwtAuthFilter,
                                                    List<SecurityRules> securityRules,
                                                    CorsConfigurationSource corsConfigurationSource) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> {
                    // Apply modular security rules
                    securityRules.forEach(rule -> rule.configure(auth));

                    // Custom public endpoints
                    auth.requestMatchers("/custom/public/**").permitAll()
                        .anyRequest().authenticated();
                })
                .exceptionHandling(exc -> exc
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
```

### Custom Bean Configuration

Override any default bean:

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

### SecurityRules Interface

```java
public interface SecurityRules {
    void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry);
}
```

Implement this interface to add modular security rules that are automatically discovered and applied.

### Password Validation Annotations

```java
@ValidPassword  // Validates password against configured policy
private String password;

@Lowercase  // Ensures string is lowercase (useful for emails)
private String email;
```

---

## 🏗️ Architecture

### Auto-Configuration

The jwt-auth-starter automatically configures:

1. **JwtService** - Token generation and parsing
2. **JwtAuthenticationFilter** - Request authentication via JWT
3. **SecurityFilterChain** - Complete Spring Security configuration with:
   - CSRF disabled (not needed for stateless JWT)
   - CORS enabled (from application.yaml)
   - Stateless session management
   - JWT filter registration
   - Modular security rules integration
   - Exception handling (401 Unauthorized)
4. **PasswordPolicy** - Configurable password validation
5. **PasswordValidator** - Bean validation for @ValidPassword
6. **CorsConfiguration** - CORS from application.yaml
7. **Method Security** - @PreAuthorize support

### Security Flow

1. Request arrives at server
2. JwtAuthenticationFilter extracts JWT from `Authorization: Bearer <token>` header
3. JwtService validates and parses token
4. User ID is set in SecurityContext as principal
5. SecurityFilterChain checks authorization rules:
   - Default public endpoints (swagger, actuator)
   - Custom SecurityRules components
   - Final fallback: authenticated required
6. If unauthorized, returns 401 Unauthorized
7. Controller accesses authenticated user via SecurityContext

---

## 🔒 Security Considerations

1. **Secret Key**: Use a strong, randomly generated secret key (minimum 256 bits)
2. **Environment Variables**: Never commit secrets to version control - use environment variables
3. **HTTPS Only**: Always use HTTPS in production for token transmission
4. **Token Storage**: Store refresh tokens in HttpOnly cookies, access tokens in memory
5. **Token Expiration**: Keep access tokens short-lived (15 minutes recommended)
6. **Account Status**: Check `isEnabled()` before issuing tokens
7. **Password Policy**: Use strong password requirements (all enabled by default)
8. **CORS**: Only allow trusted origins in production

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

See the [spring-api-starter](https://github.com/KyleRobison15/spring-api-refresher) project for a complete working example of the JWT Auth Starter integration with user management, authentication endpoints, and more.

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

### "No SecurityRules beans found"

This is normal if you haven't created any SecurityRules components. The default SecurityFilterChain will still work - it just means all endpoints (except swagger, actuator) require authentication.

### "CORS error in browser"

Ensure you've configured `cors.allowed-origins` in your application.yaml with the origin of your frontend application.

### "Password validation not working"

1. Check that `@ValidPassword` is on your password field
2. Ensure you're using `@Valid` or `@Validated` on your controller method parameter
3. Verify password policy configuration in application.yaml

### "401 Unauthorized on public endpoints"

Check your SecurityRules components - they are evaluated in the order Spring discovers them. Ensure public endpoints are configured before more restrictive rules.

---

## 🔄 Migration from Previous Version

If you're upgrading from a version without auto-configured SecurityFilterChain:

1. **Remove your SecurityConfig class** - The SecurityFilterChain is now auto-configured
2. **Create SecurityRules components** instead - Use the modular approach for custom rules
3. **Add CORS to application.yaml** - No need for custom CorsConfigurationSource
4. **Add password validation** - Use `@ValidPassword` annotation on password fields

---

## 📄 License

MIT License - see [LICENSE](../LICENSE) file for details.
