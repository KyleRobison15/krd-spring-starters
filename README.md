# KRD Spring Starters

A collection of reusable Spring Boot Starters to accelerate API development. These starters provide common functionality that can be easily integrated into any Spring Boot project with minimal configuration.

## 🚀 Available Starters

### Spring API Starter
**Artifact:** `com.krd:spring-api-starter:1.0.0`

Comprehensive Spring Boot Starter that combines web, security, data-jpa, validation, JWT authentication, and user management into a single opinionated starter for building REST APIs.

**Features:**
- JWT authentication with dual-token system
- User management with soft-delete and auto-reactivation
- Role-based access control
- Scheduled hard delete of soft-deleted users
- Base controllers and services for user/auth endpoints
- Database migrations with Flyway
- MapStruct for DTO mapping
- Includes exception-handling-starter and security-rules-starter

---

### Exception Handling Starter
**Artifact:** `com.krd:exception-handling-starter:1.0.0`

Standardized exception handling and error responses for Spring Boot microservices. Provides consistent, RFC 7807-compliant error responses across all your APIs.

**Features:**
- Auto-configured global exception handler
- Standardized ErrorResponse structure
- Handles common exceptions (validation, auth, malformed JSON, etc.)
- Zero configuration required
- Extensible for domain-specific exceptions
- Consistent error responses across all microservices

[📖 Full Documentation](./exception-handling-starter/README.md)

---

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

### Security Rules Starter
**Artifact:** `com.krd:security-rules-starter:1.0.0`

Modular security configuration pattern for organizing Spring Security authorization rules by feature. Allows different parts of your application to contribute their own security rules.

**Features:**
- SecurityRules interface for modular security configuration
- Automatic discovery and aggregation of security rules
- Clean separation of security concerns by feature
- Compatible with Spring Security's authorize-http-requests

---

### Payment Gateway Starter
**Artifact:** `com.krd:payment-gateway-starter:1.0.0`

Abstract payment gateway interface with complete Stripe integration for checkout sessions and webhook processing.

**Features:**
- PaymentGateway interface for provider-agnostic payment processing
- Complete Stripe integration (checkout sessions, webhooks)
- OrderInfo interface to decouple from domain entities
- Webhook signature verification for security
- Auto-configured security rules for webhook endpoints
- Extensible for additional payment providers (PayPal, Square, etc.)

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

## 📚 Dependency Management

This project uses a **two-tier dependency management strategy** to keep versions consistent across all starters while avoiding duplication:

### 1. Spring Boot BOM (Bill of Materials)

All **Spring Boot dependencies** are automatically versioned by importing the Spring Boot BOM in the root `build.gradle`:

```gradle
dependencyManagement {
    imports {
        mavenBom 'org.springframework.boot:spring-boot-dependencies:3.4.5'
    }
}
```

**This means you NEVER specify versions for:**
- `spring-boot-starter-*` (web, security, data-jpa, validation, etc.)
- `lombok`
- `slf4j-api`
- `spring-boot-autoconfigure`
- Any other Spring Boot managed dependencies

**Example:**
```gradle
// ✅ Correct - No version needed
api 'org.springframework.boot:spring-boot-starter-web'

// ❌ Wrong - Don't specify version (BOM handles it)
api 'org.springframework.boot:spring-boot-starter-web:3.4.5'
```

---

### 2. Version Catalog (Custom Dependencies)

All **non-Spring dependencies** are managed in `gradle/libs.versions.toml`. This provides a centralized location for version management with type-safe accessors.

**File:** `gradle/libs.versions.toml`

```toml
[versions]
jjwt = "0.12.6"
stripe = "29.0.0"
mapstruct = "1.5.5.Final"

[libraries]
jjwt-api = { module = "io.jsonwebtoken:jjwt-api", version.ref = "jjwt" }
stripe = { module = "com.stripe:stripe-java", version.ref = "stripe" }
mapstruct = { module = "org.mapstruct:mapstruct", version.ref = "mapstruct" }

[bundles]
jjwt = ["jjwt-api", "jjwt-impl", "jjwt-jackson"]
```

**Usage in module `build.gradle` files:**

```gradle
dependencies {
    // Version Catalog - Type-safe accessors
    api libs.jjwt.api
    implementation libs.stripe
    api libs.mapstruct

    // Spring Boot BOM - No version needed
    api 'org.springframework.boot:spring-boot-starter-web'
}
```

---

### 📋 How to Add a New Dependency

#### If it's a Spring Boot dependency:
**Just add it - no version needed!**

```gradle
// In any module's build.gradle
dependencies {
    api 'org.springframework.boot:spring-boot-starter-cache'  // No version!
}
```

#### If it's a custom (non-Spring) dependency:

**Step 1:** Add to `gradle/libs.versions.toml`
```toml
[versions]
my-library = "2.1.0"  # Add version here

[libraries]
my-library = { module = "com.example:my-library", version.ref = "my-library" }
```

**Step 2:** Use in any module's `build.gradle`
```gradle
dependencies {
    implementation libs.my.library  # Type-safe accessor
}
```

**Step 3:** Enjoy IDE autocomplete
- Type `libs.` and your IDE will show all available libraries
- Refactoring support - rename a library and all references update
- Single source of truth for versions

---

### 🔄 How to Update a Version

#### Updating Spring Boot version:
**Update once in root `build.gradle`:**

```gradle
dependencyManagement {
    imports {
        mavenBom 'org.springframework.boot:spring-boot-dependencies:3.5.0'  // Update here
    }
}
```

All starters automatically use the new Spring Boot versions.

#### Updating custom dependency version:
**Update once in `gradle/libs.versions.toml`:**

```toml
[versions]
stripe = "30.0.0"  # Change from 29.0.0 to 30.0.0
```

All modules using `libs.stripe` automatically get the new version.

---

### 🎯 Benefits of This Approach

1. **Single Source of Truth**
   - Spring Boot deps: Managed by BOM
   - Custom deps: Managed by version catalog
   - Update versions in ONE place, applies everywhere

2. **Type Safety**
   - IDE autocomplete for `libs.stripe`, `libs.jjwt.api`
   - Catch typos at compile time, not runtime

3. **Consistency**
   - All modules use the same version
   - No accidental version conflicts

4. **Maintainability**
   - Easy to see all dependency versions at a glance
   - Clear separation between Spring and custom dependencies

5. **Modern Gradle**
   - Version catalog is Gradle's recommended approach (7.0+)
   - Same pattern used by Spring Boot internally

---

### 📖 Reference

- **Version Catalog:** `gradle/libs.versions.toml`
- **BOM Import:** `build.gradle` (root)
- **Gradle Docs:** [Version Catalogs](https://docs.gradle.org/current/userguide/platforms.html)
- **Spring Boot BOM:** [Dependency Management](https://docs.spring.io/spring-boot/dependency-versions.html)

---

## 📋 Project Structure

```
krd-spring-starters/
├── build.gradle                    # Root build config with BOM imports
├── settings.gradle                 # Module definitions
├── gradle/
│   └── libs.versions.toml         # 📦 Version Catalog (dependency versions)
├── README.md                       # This file
│
├── spring-api-starter/             # Comprehensive API Starter
│   ├── build.gradle               # Uses: libs.jjwt.*, libs.mapstruct
│   ├── src/
│   └── README.md
│
├── exception-handling-starter/     # Exception Handling Starter
│   ├── build.gradle
│   ├── src/
│   └── README.md
│
├── jwt-auth-starter/               # JWT Authentication Starter
│   ├── build.gradle
│   ├── src/
│   └── README.md
│
├── security-rules-starter/         # Modular Security Rules
│   ├── build.gradle
│   └── src/
│
└── payment-gateway-starter/        # Payment Gateway with Stripe
    ├── build.gradle               # Uses: libs.stripe
    └── src/
```

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
