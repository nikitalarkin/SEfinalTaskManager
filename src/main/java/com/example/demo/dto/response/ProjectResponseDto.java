package com.example.demo.dto.response;

import java.time.Instant;
import java.util.Set;

public record ProjectResponseDto(
        Long id,
        String name,
        String description,
        Long createdById,
        Instant createdAt,
        Set<Long> memberIds
) {
}
