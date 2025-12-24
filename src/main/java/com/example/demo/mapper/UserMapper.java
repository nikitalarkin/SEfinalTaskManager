package com.example.demo.mapper;

import com.example.demo.dto.request.UserRequestDto;
import com.example.demo.dto.response.UserResponseDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toEntity(UserRequestDto dto);

    @Mapping(target = "roles", expression = "java(roleNames(user.getRoles()))")
    UserResponseDto toResponse(User user);

    default Set<String> roleNames(Set<Role> roles) {
        if (roles == null) return Set.of();
        return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}
