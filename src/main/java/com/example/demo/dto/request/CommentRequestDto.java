package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentRequestDto(
        @NotNull
        Long taskId,

        @NotBlank
        @Size(max = 2000)
        String text
) {
}
