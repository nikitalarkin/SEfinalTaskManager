package com.example.demo.service;

import com.example.demo.dto.request.ChangePasswordRequestDto;
import com.example.demo.dto.request.RegisterRequestDto;
import com.example.demo.dto.response.UserResponseDto;

public interface AuthService {
    UserResponseDto register(RegisterRequestDto dto);
    void changePassword(String email, ChangePasswordRequestDto dto);
}
