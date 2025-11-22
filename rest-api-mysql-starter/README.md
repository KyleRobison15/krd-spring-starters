# KRD REST API MySQL Starter

Complete Spring Boot starter for building REST APIs with MySQL database, JWT authentication, and industry best practices.

## Features

**Complete Stack Included:**
- ✅ Spring Boot Web (REST endpoints)
- ✅ Spring Security + JWT authentication (via auth-starter)
- ✅ Spring Data JPA (database operations)
- ✅ MySQL + Flyway (database migrations)
- ✅ MapStruct (DTO mapping)
- ✅ OpenAPI/Swagger (API documentation)
- ✅ Bean Validation (request validation)
- ✅ Global exception handling
- ✅ Lombok (boilerplate reduction)

**Auto-Configured:**
- JWT authentication with access/refresh tokens
- Swagger UI at `/swagger-ui.html`
- Flyway migrations on startup
- Global exception handlers for validation, access denied, etc.
- JSON serialization with sensible defaults

## Installation

Add the dependency to your `build.gradle`:

```gradle
dependencies {
    implementation 'com.krd:rest-api-mysql-starter:1.0.0'
}
```

That's it! No other Spring Boot dependencies needed.

## Quick Start

### 1. Configure Database

Add to your `application.properties`:

```properties
# Database connection
spring.datasource.url=jdbc:mysql://localhost:3306/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT Configuration
jwt.secret=your-256-bit-secret-key-change-in-production
jwt.access-token-expiration=900000
jwt.refresh-token-expiration=604800000
```

### 2. Create Your User Entity

Extend `BaseUser` from auth-starter:

```java
@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class User extends BaseUser {
    // BaseUser provides: id, firstName, lastName, email, password, roles, addresses

    // Add custom fields for your domain
    private String phoneNumber;
    private LocalDate dateOfBirth;
}
```

### 3. Create Repository

```java
@Repository
public interface UserRepository extends BaseUserRepository<User> {
    // Common queries inherited from BaseUserRepository
    // Add custom queries here
}
```

### 4. Create UserService

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

### 5. Create UserDetailsService

```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

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

### 6. Create Auth Controller

```java
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController extends BaseAuthController<User> {

    public AuthController(UserService userService) {
        super(userService);
    }

    // Inherited endpoints:
    // POST /api/auth/register
    // POST /api/auth/login
    // POST /api/auth/refresh
    // POST /api/auth/logout
}
```

### 7. Create Your Domain Entities

Standard Spring Data JPA entities:

```java
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
```

### 8. Create Flyway Migrations

Add migrations in `src/main/resources/db/migration/`:

```sql
-- V1__Create_users_table.sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE user_roles (
    user_id BIGINT,
    role VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- V2__Create_products_table.sql
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL
);
```

### 9. Run Your Application

```bash
./gradlew bootRun
```

Your API is now running with:
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs
- Auth endpoints: http://localhost:8080/api/auth/*

## What You Get Out of the Box

### Authentication Endpoints

All provided by `BaseAuthController`:

```
POST /api/auth/register - Register new user
POST /api/auth/login    - Login and get JWT token
POST /api/auth/refresh  - Refresh access token
POST /api/auth/logout   - Logout (clear refresh token)
```

### Global Exception Handling

Automatically handles:
- Validation errors (`@Valid` failures)
- Malformed JSON
- Access denied (403)
- Generic runtime exceptions (500)

### OpenAPI/Swagger

- Interactive API documentation at `/swagger-ui.html`
- JWT authentication pre-configured
- Try-it-out enabled for testing

### Flyway Migrations

- Runs automatically on startup
- Validates migrations
- Baselines existing databases
- Tracks migration history

### MapStruct

Annotation processor configured - just create mapper interfaces:

```java
@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDto toDto(Product product);
    Product toEntity(ProductDto dto);
}
```

## Architecture

This starter follows a layered architecture:

```
Controller Layer (REST endpoints)
    ↓
Service Layer (Business logic)
    ↓
Repository Layer (Data access)
    ↓
Database (MySQL)
```

**Best Practices Included:**
- Stateless JWT authentication
- DTO pattern (separate entities from API contracts)
- Repository pattern (Spring Data JPA)
- Migration-based schema management (Flyway)
- Global exception handling
- API documentation (OpenAPI)
- Request validation (Bean Validation)

## Customization

### Override Security Configuration

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Customize security rules here
        return http.build();
    }
}
```

### Add CORS Configuration

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("http://localhost:3000"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### Customize OpenAPI Info

Override the `OpenAPI` bean in your configuration.

### Add Custom Exception Handlers

Extend `GlobalExceptionHandler` or create your own `@RestControllerAdvice`.

## Dependencies Included

- Spring Boot 3.4.5
- Spring Security + JWT
- Spring Data JPA
- MySQL Connector
- Flyway Core + MySQL
- MapStruct 1.6.3
- SpringDoc OpenAPI 2.8.6
- Lombok
- Bean Validation

## Example Projects

See the `examples/` directory for complete sample projects.

## License

MIT
