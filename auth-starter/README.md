# KRD Auth Starter

Spring Boot starter for JWT-based authentication with an extensible user model.

## Features

- JWT authentication with access and refresh tokens
- Extensible `BaseUser` entity using `@MappedSuperclass`
- Generic services and repositories
- Auto-configured security with Spring Security
- HttpOnly cookie support for refresh tokens
- Customizable token expiration
- Built-in endpoints for login, register, refresh, and logout

## Installation

This starter requires Spring Boot Web, Data JPA, and Validation to be provided by your application.

**Option 1: Use with rest-api-mysql-starter (Recommended)**

```gradle
dependencies {
    implementation 'com.krd:rest-api-mysql-starter:1.0.0'
    // Includes auth-starter + all required dependencies
}
```

**Option 2: Standalone usage**

```gradle
dependencies {
    implementation 'com.krd:auth-starter:1.0.0'

    // Required dependencies (must be provided)
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // Database driver (your choice)
    runtimeOnly 'com.mysql:mysql-connector-j'  // or PostgreSQL, H2, etc.
}
```

## Quick Start

### 1. Create Your User Entity

Extend `BaseUser` to create your custom user entity:

```java
@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class User extends BaseUser {
    // Add custom fields here
    private String phoneNumber;
    private LocalDate dateOfBirth;
}
```

### 2. Create Your Repository

```java
@Repository
public interface UserRepository extends BaseUserRepository<User> {
    // Add custom queries here
}
```

### 3. Create Your Service

```java
@Service
public class UserService extends BaseUserService<User> {

    public UserService(
            UserRepository repository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService
    ) {
        super(repository, passwordEncoder, jwtService, authenticationManager, userDetailsService);
    }

    @Override
    protected User createUserFromRequest(RegisterRequest request) {
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        user.addRole(Role.ROLE_USER);
        return user;
    }
}
```

### 4. Create Your UserDetailsService

```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.name()))
                        .toList())
                .build();
    }
}
```

### 5. Create Your Controller

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController extends BaseAuthController<User> {

    public AuthController(UserService userService) {
        super(userService);
    }
}
```

### 6. Configure Application Properties

```properties
# JWT Secret (IMPORTANT: Use a strong secret in production!)
jwt.secret=your-256-bit-secret-key-change-this-in-production

# Token expiration times
jwt.access-token-expiration=900000       # 15 minutes
jwt.refresh-token-expiration=604800000   # 7 days

# Database configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/yourdb
spring.datasource.username=youruser
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```

## API Endpoints

The starter provides the following endpoints:

### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

### Refresh Token
```http
POST /api/auth/refresh
```

### Logout
```http
POST /api/auth/logout
```

## Customization

### Custom Security Configuration

Override the security filter chain in your application:

```java
@Configuration
public class CustomSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Your custom security configuration
        return http.build();
    }
}
```

### CORS Configuration

Add CORS configuration to allow your frontend:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("http://localhost:5173"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

## BaseUser Fields

The `BaseUser` entity includes:

- `id` - Primary key
- `firstName` - User's first name
- `lastName` - User's last name
- `email` - User's email (unique)
- `password` - Encrypted password
- `roles` - Set of user roles
- `addresses` - Set of user addresses
- `createdAt` - Timestamp of creation
- `updatedAt` - Timestamp of last update

## License

MIT
