# Spring API Starter - Development Roadmap

## Completed Features ✅

### Phase 1: Project Setup
- [x] Design spring-api-starter structure and plan extraction
- [x] Create spring-api-starter project skeleton in krd-spring-starters
- [x] Set up Gradle build configuration with Spring Boot dependencies

### Phase 2: Core Authentication & Security
- [x] Extract and adapt JWT authentication code (JwtService, JwtAuthenticationFilter, JwtConfig)
- [x] Extract JWT DTOs (LoginRequest, LoginResponse, JwtResponse, Jwt)
- [x] Create auto-configuration class (SpringApiStarterAutoConfiguration)
- [x] Configure Spring Security with JWT-based stateless authentication
- [x] Set up BCrypt password encoder

### Phase 3: User Management
- [x] Extract and adapt User management code (BaseUser, BaseUserRepository, BaseUserService)
- [x] Extract UserController endpoints into BaseUserController
- [x] Implement soft-delete functionality
- [x] Implement role management (add/remove roles with audit logging)
- [x] Create RoleChangeLog entity and repository for audit trail
- [x] Add user CRUD operations (get, update, delete, change password)

### Phase 4: Validation
- [x] Extract password validation and custom validators
- [x] Create configurable PasswordPolicy
- [x] Create custom validators (ValidPassword, Lowercase)
- [x] Add validation to all DTOs (RegisterUserRequest, UpdateUserRequest, ChangePasswordRequest)

### Phase 5: Database Migrations
- [x] Create Flyway migrations for users table
- [x] Create Flyway migrations for role_change_logs table

### Phase 6: Testing & Integration
- [x] Publish to Maven local and test in spring-api-with-krd-starters
- [x] Refactor spring-api-with-krd-starters to use the new starter
- [x] Fix parameter name resolution error in BaseUserController
- [x] Fix soft-delete duplicate entry bug (Approach 2: email-only modification with auto-reactivation)
- [x] Fix username validation to check soft-deleted users during registration

### Phase 7: Authentication Endpoints
- [x] Extract AuthService into BaseAuthService
- [x] Extract AuthController into BaseAuthController
- [x] Implement login endpoint with cookie-based refresh token
- [x] Implement refresh token endpoint
- [x] Implement /me endpoint for getting current user
- [x] Refactor consumer project to extend base auth classes

## In Progress 🚧

### Phase 8: Hard Delete Feature
- [ ] Add configurable hard delete after X days for soft-deleted users
- [ ] Create scheduled task to automatically hard delete old soft-deleted users
- [ ] Add configuration properties for hard delete threshold
- [ ] Add admin endpoint to manually trigger hard delete for specific users
- [ ] Update documentation with hard delete behavior

## Planned Features 📋

### Phase 9: Template Project
- [ ] Create GitHub template project with spring-api-starter
- [ ] Set up example application structure
- [ ] Add sample configuration files (application.yml, application-dev.yml)
- [ ] Create example User, UserDto, UserService, UserController implementations
- [ ] Add example AuthService and AuthController implementations
- [ ] Include README with setup instructions

### Phase 10: Payment Gateway (Optional)
- [ ] Extract payment gateway into payment-gateway-starter
- [ ] Create BasePaymentGateway interface
- [ ] Implement StripePaymentGateway
- [ ] Add payment-related DTOs and entities
- [ ] Create Flyway migrations for payment tables

### Phase 11: Documentation
- [ ] Update CLAUDE.md with starter usage instructions
- [ ] Create comprehensive README for spring-api-starter
- [ ] Add JavaDoc comments to all public APIs
- [ ] Create migration guide for existing projects
- [ ] Add architecture diagrams

## Future Enhancements 💡

### Security Enhancements
- [ ] Add email verification during registration
- [ ] Implement password reset functionality
- [ ] Add account lockout after failed login attempts
- [ ] Implement 2FA (Two-Factor Authentication)

### User Management Enhancements
- [ ] Add user profile picture support
- [ ] Implement user activity logging
- [ ] Add user preferences/settings entity
- [ ] Support for OAuth2 providers (Google, GitHub, etc.)

### Performance & Monitoring
- [ ] Add Redis caching for JWT tokens
- [ ] Implement rate limiting
- [ ] Add metrics and monitoring endpoints
- [ ] Create health check endpoints

### Developer Experience
- [ ] Add Spring Boot Actuator integration
- [ ] Create OpenAPI/Swagger documentation auto-configuration
- [ ] Add development mode with auto-seeding
- [ ] Create CLI tool for generating boilerplate code

## Notes

### Soft Delete Behavior
The current implementation uses Approach 2 for soft deletes:
- When a user is deleted, only the email is appended with `_deleted`
- Username remains unchanged to preserve the user's identity claim
- Deleted users are automatically reactivated if they re-register with the same email
- This prevents username collision issues while maintaining simple auto-reactivation logic

### Architecture Decisions
- Generic base classes pattern for maximum flexibility
- Spring Boot Auto-Configuration for zero-configuration experience
- Soft delete by default with optional hard delete feature
- JWT-based stateless authentication
- Method-level security with `@PreAuthorize` annotations
