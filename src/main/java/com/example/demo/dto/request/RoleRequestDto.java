package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleRequestDto(
        @NotBlank
        @Size(max = 32)
        String name
) {
}
