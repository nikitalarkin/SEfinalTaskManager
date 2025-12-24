package com.example.demo.service;

import com.example.demo.dto.request.UserRequestDto;
import com.example.demo.dto.response.UserResponseDto;

import java.util.Set;

public interface AdminUserService {
    UserResponseDto createUser(UserRequestDto dto);
    void setActive(Long userId, boolean active);
    void deleteUser(Long userId);
    UserResponseDto setRoles(Long userId, Set<String> roles);
}
