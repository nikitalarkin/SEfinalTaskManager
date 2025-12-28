package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private boolean isActive;
        private Set<String> roles;
}
