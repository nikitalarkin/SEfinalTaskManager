package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequestDto {
        @NotBlank
        @Size(min = 6, max = 72)
        private String oldPassword;

        @NotBlank
        @Size(min = 6, max = 72)
        private String newPassword;
}
