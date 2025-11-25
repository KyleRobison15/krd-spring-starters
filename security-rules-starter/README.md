# Security Rules Starter

A Spring Boot starter that provides a modular approach to organizing Spring Security authorization rules by feature or module.

## Overview

The Security Rules Starter enables you to define authorization rules for different features of your application in a decentralized, modular way. Instead of having all security rules in one large configuration file, each feature can define its own security rules in a separate class.

## Benefits

- **Modular Organization**: Keep security rules with the features they protect
- **Separation of Concerns**: Each module owns its security configuration
- **Easy Maintenance**: Adding/removing features doesn't require changes to central security config
- **Auto-Discovery**: Spring automatically finds and applies all security rule implementations
- **Better Readability**: Small, focused security rule classes are easier to understand than one large config

## Installation

### Add Dependency

Add the starter to your `build.gradle`:

```gradle
dependencies {
    implementation 'com.krd:security-rules-starter:1.0.0'
}
```

Or if using Maven (`pom.xml`):

```xml
<dependency>
    <groupId>com.krd</groupId>
    <artifactId>security-rules-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

### 1. Create Security Rule Implementations

Create a class that implements `SecurityRules` for each feature or module:

```java
package com.example.products;

import com.krd.security.SecurityRules;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class ProductSecurityRules implements SecurityRules {

    @Override
    public void configure(AuthorizationManagerRequestMatcherRegistry registry) {
        registry.requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/products/**").hasRole("ADMIN");
    }
}
```

```java
package com.example.auth;

import com.krd.security.SecurityRules;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class AuthSecurityRules implements SecurityRules {

    @Override
    public void configure(AuthorizationManagerRequestMatcherRegistry registry) {
        registry.requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll();
    }
}
```

### 2. Update Security Configuration

In your main `SecurityConfig`, inject all security rules and apply them:

```java
package com.example.config;

import com.krd.security.SecurityRules;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final List<SecurityRules> securityRules;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(c -> {
                // Apply all discovered security rules
                securityRules.forEach(rule -> rule.configure(c));

                // Default rule - all other requests require authentication
                c.anyRequest().authenticated();
            })
            // ... other security configuration (CORS, CSRF, filters, etc.)
            ;

        return http.build();
    }
}
```

That's it! The starter will automatically discover all `SecurityRules` implementations in your application and make them available for injection.

## How It Works

1. **Interface**: The `SecurityRules` interface defines a single method that receives Spring Security's authorization registry
2. **Implementation**: Each feature implements `SecurityRules` and is annotated with `@Component`
3. **Auto-Discovery**: The starter's auto-configuration enables component scanning to find all implementations
4. **Injection**: Spring automatically collects all implementations and injects them as a `List<SecurityRules>`
5. **Application**: Your `SecurityConfig` iterates through the list and applies each rule

## Best Practices

### Organize by Feature

Keep security rules close to the code they protect:

```
src/main/java/com/example/
├── products/
│   ├── ProductController.java
│   ├── ProductService.java
│   └── ProductSecurityRules.java
├── users/
│   ├── UserController.java
│   ├── UserService.java
│   └── UserSecurityRules.java
└── auth/
    ├── AuthController.java
    └── AuthSecurityRules.java
```

### Keep Rules Simple

Each implementation should focus on one feature's authorization rules:

```java
// Good - focused on one feature
@Component
public class UserSecurityRules implements SecurityRules {
    @Override
    public void configure(AuthorizationManagerRequestMatcherRegistry registry) {
        registry.requestMatchers(HttpMethod.GET, "/users/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/users").permitAll()  // Registration
                .requestMatchers(HttpMethod.PUT, "/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN");
    }
}
```

### Use Descriptive Class Names

Name your security rules classes clearly to indicate what they protect:

- `ProductSecurityRules` - rules for product endpoints
- `AuthSecurityRules` - rules for authentication endpoints
- `AdminSecurityRules` - rules for admin endpoints
- `SwaggerSecurityRules` - rules for API documentation endpoints

### Order Matters

The order in which security rules are applied can matter if you have overlapping patterns. More specific patterns should come before more general ones. The rules are applied in the order they are returned by Spring's dependency injection.

If you need to control the order, use `@Order` annotation:

```java
@Component
@Order(1)  // Applied first
public class AuthSecurityRules implements SecurityRules { ... }

@Component
@Order(2)  // Applied second
public class ProductSecurityRules implements SecurityRules { ... }
```

## Advanced Usage

### Conditional Security Rules

You can use Spring's conditional annotations to enable/disable security rules:

```java
@Component
@ConditionalOnProperty(name = "feature.admin.enabled", havingValue = "true")
public class AdminSecurityRules implements SecurityRules {
    @Override
    public void configure(AuthorizationManagerRequestMatcherRegistry registry) {
        registry.requestMatchers("/admin/**").hasRole("ADMIN");
    }
}
```

### Profile-Specific Rules

Use `@Profile` to apply different rules in different environments:

```java
@Component
@Profile("dev")
public class DevSecurityRules implements SecurityRules {
    @Override
    public void configure(AuthorizationManagerRequestMatcherRegistry registry) {
        // In dev, allow access to H2 console
        registry.requestMatchers("/h2-console/**").permitAll();
    }
}
```

## Common Patterns

### Public Endpoints

```java
registry.requestMatchers(HttpMethod.GET, "/products/**").permitAll()
```

### Authenticated Endpoints

```java
registry.requestMatchers("/profile/**").authenticated()
```

### Role-Based Access

```java
registry.requestMatchers("/admin/**").hasRole("ADMIN")
```

### Multiple Roles

```java
registry.requestMatchers("/reports/**").hasAnyRole("ADMIN", "MANAGER")
```

### Method-Specific Rules

```java
registry.requestMatchers(HttpMethod.GET, "/products/**").permitAll()
        .requestMatchers(HttpMethod.POST, "/products/**").hasRole("ADMIN")
```

## Troubleshooting

### Rules Not Being Applied

**Problem**: Your security rules implementation is not being applied.

**Solutions**:
1. Ensure the class is annotated with `@Component`
2. Verify the class is in a package that Spring scans
3. Check that the starter dependency is included
4. Verify you're injecting `List<SecurityRules>` in your `SecurityConfig`

### Rules Applied in Wrong Order

**Problem**: Security rules are not being applied in the expected order.

**Solution**: Use `@Order` annotation to control the order:

```java
@Component
@Order(1)
public class MySecurityRules implements SecurityRules { ... }
```

Lower numbers have higher priority and are applied first.

### Conflicting Rules

**Problem**: Multiple security rules define authorization for the same endpoint pattern.

**Solution**:
1. Make patterns more specific
2. Consolidate related rules into a single implementation
3. Use `@Order` to control which rule takes precedence

## API Reference

### SecurityRules Interface

```java
public interface SecurityRules {
    void configure(AuthorizationManagerRequestMatcherRegistry registry);
}
```

**Method**: `configure(AuthorizationManagerRequestMatcherRegistry registry)`
- **Purpose**: Define authorization rules for HTTP requests
- **Parameter**: Spring Security's authorization registry
- **Returns**: void

## Examples

See the [spring-api-with-krd-starters](https://github.com/KyleRobison15/spring-api-with-krd-starters) project for a complete working example.

## Version History

- **1.0.0** - Initial release with core SecurityRules interface and auto-configuration

## License

This starter is part of the KRD Spring Starters collection and is licensed under the MIT License.
