package com.krd.starter.user;

import com.krd.starter.user.dto.BaseUserDto;
import com.krd.starter.user.dto.RegisterUserRequest;
import com.krd.starter.user.dto.UpdateUserRequest;
import org.mapstruct.MappingTarget;

/**
 * Base mapper interface for user entities and DTOs.
 * <p>
 * Consumer applications should create a concrete implementation using MapStruct:
 * <pre>
 * {@code
 * @Mapper(componentModel = "spring")
 * public interface UserMapper extends BaseUserMapper<User, UserDto> {
 *     // MapStruct will generate implementations of inherited methods
 * }
 * }
 * </pre>
 *
 * @param <T> The concrete user entity type extending BaseUser
 * @param <D> The concrete user DTO type extending BaseUserDto
 */
public interface BaseUserMapper<T extends BaseUser, D extends BaseUserDto> {

    /**
     * Convert a user entity to a DTO.
     */
    D toDto(T user);

    /**
     * Convert a registration request to a user entity.
     */
    T toEntity(RegisterUserRequest request);

    /**
     * Update an existing user entity from an update request.
     */
    void update(UpdateUserRequest request, @MappingTarget T user);
}
