package com.example.demo.controller;

import com.example.demo.dto.request.UserRequestDto;
import com.example.demo.dto.response.UserResponseDto;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> me(Authentication authentication) {
        return ResponseEntity.ok(userService.getMe(authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateMe(Authentication authentication,
                                                    @Valid @RequestBody UserRequestDto dto) {
        return ResponseEntity.ok(userService.updateMe(authentication.getName(), dto));
    }
}
