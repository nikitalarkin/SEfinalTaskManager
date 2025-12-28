package com.example.demo.service.impl;

import com.example.demo.dto.request.ChangePasswordRequestDto;
import com.example.demo.dto.request.RegisterRequestDto;
import com.example.demo.dto.response.UserResponseDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    UserMapper userMapper;

    @InjectMocks
    AuthServiceImpl authService;

    @Test
    void register_success_assignsUserRole_andHashesPassword() {
        RegisterRequestDto dto = new RegisterRequestDto("user@test.com", "password123", "Test", "User");
        Role roleUser = new Role();
        roleUser.setId(1L);
        roleUser.setName("USER");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenReturn(
                new UserResponseDto(10L, dto.getEmail(), dto.getFirstName(), dto.getLastName(), true, Set.of("USER")));

        UserResponseDto resp = authService.register(dto);

        assertEquals(dto.getEmail(), resp.getEmail());
        assertTrue(resp.getRoles().contains("USER"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals(dto.getEmail(), saved.getEmail());
        assertEquals("hashed", saved.getPasswordHash());
        assertTrue(saved.isActive());
        assertTrue(saved.getRoles().stream().anyMatch(r -> r.getName().equals("USER")));
    }

    @Test
    void register_fails_whenEmailExists() {
        RegisterRequestDto dto = new RegisterRequestDto("user@test.com", "password123", "Test", "User");
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_success_updatesHash() {
        String email = "user@test.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setPasswordHash("oldHash");
        user.setActive(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "oldHash")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("newHash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.changePassword(email, new ChangePasswordRequestDto("oldPass", "newPass123"));

        assertEquals("newHash", user.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_fails_whenOldPasswordWrong() {
        String email = "user@test.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setPasswordHash("oldHash");
        user.setActive(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOld", "oldHash")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> authService.changePassword(email, new ChangePasswordRequestDto("wrongOld", "newPass123")));
        verify(userRepository, never()).save(any());
    }
}
