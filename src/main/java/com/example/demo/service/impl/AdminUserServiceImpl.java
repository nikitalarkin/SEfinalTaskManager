package com.example.demo.service.impl;

import com.example.demo.dto.request.UserRequestDto;
import com.example.demo.dto.response.UserResponseDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AdminUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public AdminUserServiceImpl(UserRepository userRepository,
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
    public UserResponseDto createUser(UserRequestDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .email(dto.email())
                .passwordHash(passwordEncoder.encode(dto.password()))
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .isActive(dto.isActive() != null ? dto.isActive() : true)
                .build();

        Set<String> roleNames = dto.roles() == null || dto.roles().isEmpty() ? Set.of("USER") : dto.roles();
        Set<Role> roles = roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + name)))
                .collect(Collectors.toSet());

        user.setRoles(roles);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    @Override
    public void setActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setActive(active);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(userId);
    }

    @Transactional
    @Override
    public UserResponseDto setRoles(Long userId, Set<String> roles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<Role> roleEntities = roles.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + name)))
                .collect(Collectors.toSet());

        user.setRoles(roleEntities);
        return userMapper.toResponse(userRepository.save(user));
    }
}
