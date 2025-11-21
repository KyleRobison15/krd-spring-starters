# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2025-11-21

### Added

#### Auth Starter
- Initial release of auth-starter module
- JWT authentication with access tokens (15 minutes expiration)
- JWT refresh tokens (7 days expiration) stored in httpOnly cookies
- Extensible `BaseUser` entity as `@MappedSuperclass`
  - Includes: firstName, lastName, email, password, roles, addresses
  - Timestamps: createdAt, updatedAt
- Generic `BaseUserRepository<T extends BaseUser>` with common queries
  - `findByEmail(String email)`
  - `existsByEmail(String email)`
- Generic `BaseUserService<T extends BaseUser>` with authentication operations
  - User registration
  - User login
  - Token refresh
  - Logout
- `BaseAuthController<T extends BaseUser>` with REST endpoints
  - POST `/register` - Register new user
  - POST `/login` - Login user
  - POST `/refresh` - Refresh access token
  - POST `/logout` - Logout user
- `JwtService` for token generation and validation
- `JwtAuthenticationFilter` for request interception
- Spring Security auto-configuration
- Role-based access control with `Role` enum
- `Address` embeddable entity for user addresses
- Configurable JWT settings via application.properties
  - `jwt.secret`
  - `jwt.access-token-expiration`
  - `jwt.refresh-token-expiration`
- Comprehensive documentation in README files
- Example implementations in documentation

### Security
- BCrypt password encoding
- HttpOnly cookies for refresh token storage
- CSRF protection disabled (stateless JWT)
- Session management set to STATELESS

[Unreleased]: https://github.com/KyleRobison15/krd-spring-starters/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/KyleRobison15/krd-spring-starters/releases/tag/v1.0.0
