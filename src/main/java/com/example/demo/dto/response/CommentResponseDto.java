package com.example.demo.dto.response;

import java.time.Instant;

public record CommentResponseDto(
        Long id,
        Long taskId,
        Long authorId,
        String text,
        Instant createdAt
) {
}
