# KRD Spring Boot Starters

A collection of reusable Spring Boot starters to accelerate development of Spring Boot applications with common functionality like JWT authentication, extensible user models, and more.

## Available Starters

### Auth Starter (`auth-starter`)

JWT-based authentication with an extensible user model.

**Features:**
- JWT authentication with access and refresh tokens
- Extensible `BaseUser` entity using `@MappedSuperclass`
- Generic services and repositories
- Auto-configured Spring Security
- HttpOnly cookie support for refresh tokens
- Built-in REST endpoints for auth operations

**Documentation:** See [auth-starter/README.md](auth-starter/README.md)

## Getting Started

### Prerequisites

- Java 21 or higher
- Gradle 8.11+ (or use included wrapper)
- Spring Boot 3.4.5+

### Installation

#### Using Local Maven Repository

1. Clone this repository:
```bash
git clone https://github.com/KyleRobison15/krd-spring-starters.git
cd krd-spring-starters
```

2. Publish to local Maven repository:
```bash
./gradlew publishToMavenLocal
```

3. Add the dependency to your project's `build.gradle`:
```gradle
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation 'com.krd:auth-starter:1.0.0'
}
```

## Quick Start Example

Here's a minimal example of using the auth-starter:

### 1. Add Dependencies

```gradle
dependencies {
    implementation 'com.krd:auth-starter:1.0.0'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'org.postgresql:postgresql'
}
```

### 2. Create Your User Entity

```java
@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class User extends BaseUser {
    // Add your custom fields
    private String phoneNumber;
}
```

### 3. Create Repository

```java
@Repository
public interface UserRepository extends BaseUserRepository<User> {
}
```

### 4. Create Service

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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
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

### 6. Create Controller

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController extends BaseAuthController<User> {

    public AuthController(UserService userService) {
        super(userService);
    }
}
```

### 7. Configure Properties

```properties
# JWT Configuration
jwt.secret=your-256-bit-secret-key-change-this-in-production
jwt.access-token-expiration=900000
jwt.refresh-token-expiration=604800000

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=user
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
```

That's it! Your application now has complete JWT authentication with login, register, refresh, and logout endpoints.

## Building from Source

Build all modules:
```bash
./gradlew build
```

Publish to local Maven:
```bash
./gradlew publishToMavenLocal
```

Run tests:
```bash
./gradlew test
```

## Project Structure

```
krd-spring-starters/
├── auth-starter/              # JWT authentication starter
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/krd/auth/
│   │   │   │   ├── config/    # Auto-configuration
│   │   │   │   ├── controller/# Base controllers
│   │   │   │   ├── dto/       # Request/response DTOs
│   │   │   │   ├── model/     # BaseUser and related models
│   │   │   │   ├── repository/# Generic repositories
│   │   │   │   ├── security/  # JWT filters and services
│   │   │   │   └── service/   # Generic services
│   │   │   └── resources/
│   │   └── test/
│   └── build.gradle
├── build.gradle               # Root build configuration
├── settings.gradle            # Module declarations
└── README.md                  # This file
```

## Design Philosophy

### Extensibility First

The starters are designed to be extended, not forked. Use inheritance and composition to customize:

- **Models:** Extend `BaseUser` to add custom fields
- **Services:** Extend `BaseUserService` to add custom business logic
- **Controllers:** Extend `BaseAuthController` to add custom endpoints
- **Security:** Override beans to customize security configuration

### Auto-Configuration

Starters use Spring Boot's auto-configuration to work out of the box:

- Minimal configuration required
- Sensible defaults provided
- Easy to override when needed

### Type Safety

Generic types ensure compile-time safety:

```java
BaseUserService<T extends BaseUser>
BaseUserRepository<T extends BaseUser>
BaseAuthController<T extends BaseUser>
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

MIT License - see LICENSE file for details

## Author

Kyle Robison - [GitHub](https://github.com/KyleRobison15)

## Support

For issues, questions, or contributions, please open an issue on GitHub.
