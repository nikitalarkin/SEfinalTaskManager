package com.example.demo.controller;

import com.example.demo.dto.request.UserRequestDto;
import com.example.demo.dto.response.UserResponseDto;
import com.example.demo.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> create(@Valid @RequestBody UserRequestDto dto) {
        return ResponseEntity.ok(adminUserService.createUser(dto));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> setActive(@PathVariable Long id, @RequestParam boolean active) {
        adminUserService.setActive(id, active);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<UserResponseDto> setRoles(@PathVariable Long id, @RequestBody Set<String> roles) {
        return ResponseEntity.ok(adminUserService.setRoles(id, roles));
    }
}
