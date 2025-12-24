package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDto(
        @NotBlank @Size(min = 6, max = 72) String oldPassword,
        @NotBlank @Size(min = 6, max = 72) String newPassword
) {
}
