package com.example.demo.service;

import com.example.demo.dto.request.UserRequestDto;
import com.example.demo.dto.response.UserResponseDto;

public interface UserService {
    UserResponseDto getMe(String email);
    UserResponseDto updateMe(String email, UserRequestDto dto);
}
