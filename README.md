# KRD Spring Starters

A collection of reusable Spring Boot Starters to accelerate API development. These starters provide common functionality that can be easily integrated into any Spring Boot project with minimal configuration.

## 🚀 Available Starters

### JWT Auth Starter
**Artifact:** `com.krd:jwt-auth-starter:1.0.0`

Spring Boot Starter for JWT Authentication with a dual-token system (access + refresh tokens). Provides automatic JWT token generation, parsing, and validation with minimal configuration.

**Features:**
- Dual-token authentication (access + refresh tokens)
- JwtUser interface for easy User entity integration
- Auto-configured JwtService and JwtAuthenticationFilter
- Multi-role authorization support
- Customizable token expiration times
- Account status management (enabled/disabled users)

[📖 Full Documentation](./jwt-auth-starter/README.md)

---

## 📦 Installation

### Prerequisites
- Java 21+
- Spring Boot 3.4.5+
- Gradle 8.11+ or Maven 3.9+

### Add Maven Local Repository

**Gradle:**
```gradle
repositories {
    mavenLocal()
    mavenCentral()
}
```

**Maven:**
```xml
<repositories>
    <repository>
        <id>local</id>
        <url>file://${user.home}/.m2/repository</url>
    </repository>
</repositories>
```

### Add Starter Dependency

See individual starter documentation for specific installation instructions.

---

## 🛠️ Development

### Build All Starters
```bash
./gradlew build
```

### Publish to Maven Local
```bash
./gradlew publishToMavenLocal
```

### Build Specific Starter
```bash
./gradlew :jwt-auth-starter:build
```

### Publish Specific Starter
```bash
./gradlew :jwt-auth-starter:publishToMavenLocal
```

---

## 📋 Project Structure

```
krd-spring-starters/
├── build.gradle                 # Root configuration
├── settings.gradle              # Module definitions
├── README.md                    # This file
│
├── jwt-auth-starter/            # JWT Authentication Starter
│   ├── build.gradle
│   ├── src/
│   └── README.md
│
└── (future-starter)/            # Future starters...
```

---

## 🎯 Future Starters (Planned)

### Security Rules Starter
Modular security configuration pattern for organizing authorization rules by feature.

### Payment Gateway Starter
Abstract payment gateway interface with Stripe implementation.

---

## 🤝 Contributing

This is a personal project for reusable Spring Boot components. Feel free to fork and adapt for your own needs.

---

## 📄 License

MIT License - see [LICENSE](./LICENSE) file for details.

---

## 👤 Author

**Kyle Robison**
- GitHub: [@KyleRobison15](https://github.com/KyleRobison15)
