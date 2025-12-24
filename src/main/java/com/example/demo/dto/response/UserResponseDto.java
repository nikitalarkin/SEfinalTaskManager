package com.example.demo.dto.response;

import java.util.Set;

public record UserResponseDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        boolean isActive,
        Set<String> roles
) {
}
