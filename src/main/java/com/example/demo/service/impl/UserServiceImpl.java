package com.example.demo.service.impl;

import com.example.demo.dto.request.UserRequestDto;
import com.example.demo.dto.response.UserResponseDto;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public UserResponseDto getMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userMapper.toResponse(user);
    }

    @Transactional
    @Override
    public UserResponseDto updateMe(String email, UserRequestDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (dto.firstName() != null) user.setFirstName(dto.firstName());
        if (dto.lastName() != null) user.setLastName(dto.lastName());
        if (dto.isActive() != null) user.setActive(dto.isActive());

        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }
}
