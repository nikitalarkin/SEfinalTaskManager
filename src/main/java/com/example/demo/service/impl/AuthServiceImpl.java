package com.example.demo.service.impl;

import com.example.demo.dto.request.ChangePasswordRequestDto;
import com.example.demo.dto.request.RegisterRequestDto;
import com.example.demo.dto.response.UserResponseDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Transactional
    @Override
    public UserResponseDto register(RegisterRequestDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Role USER not found"));

        User user = User.builder()
                .email(dto.email())
                .passwordHash(passwordEncoder.encode(dto.password()))
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .isActive(true)
                .build();

        user.getRoles().add(userRole);

        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Transactional
    @Override
    public void changePassword(String email, ChangePasswordRequestDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(dto.oldPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
    }
}
