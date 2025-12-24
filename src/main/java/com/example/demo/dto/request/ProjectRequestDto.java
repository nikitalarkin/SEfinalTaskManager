package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ProjectRequestDto(
        @NotBlank
        @Size(max = 200)
        String name,

        @Size(max = 2000)
        String description,

        Set<Long> memberIds
) {
}
