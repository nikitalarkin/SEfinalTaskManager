package com.example.demo.controller;

import com.example.demo.dto.request.ChangePasswordRequestDto;
import com.example.demo.dto.request.RegisterRequestDto;
import com.example.demo.dto.response.UserResponseDto;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterRequestDto dto) {
        return ResponseEntity.ok(authService.register(dto));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(Authentication authentication,
                                               @Valid @RequestBody ChangePasswordRequestDto dto) {
        authService.changePassword(authentication.getName(), dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("ok");
    }
}
