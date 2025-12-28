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
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        Set<String> roleNames = dto.getRoles() == null || dto.getRoles().isEmpty() ? Set.of("USER") : dto.getRoles();
        Set<Role> roles = new java.util.HashSet<>();
        for (String name : roleNames) {
            java.util.Optional<Role> roleOpt = roleRepository.findByName(name);
            if (roleOpt.isEmpty()) {
                throw new IllegalArgumentException("Role not found: " + name);
            }
            Role role = roleOpt.get();
            roles.add(role);
        }

        user.setRoles(roles);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    @Override
    public void setActive(Long userId, boolean active) {
        java.util.Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userOpt.get();
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
        java.util.Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userOpt.get();

        Set<Role> roleEntities = new java.util.HashSet<>();
        for (String name : roles) {
            java.util.Optional<Role> roleOpt = roleRepository.findByName(name);
            if (roleOpt.isEmpty()) {
                throw new IllegalArgumentException("Role not found: " + name);
            }
            Role role = roleOpt.get();
            roleEntities.add(role);
        }

        user.setRoles(roleEntities);
        return userMapper.toResponse(userRepository.save(user));
    }
}
