package com.example.demo.controller;

import com.example.demo.dto.request.ProjectRequestDto;
import com.example.demo.dto.response.ProjectResponseDto;
import com.example.demo.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<ProjectResponseDto> create(Authentication authentication, @Valid @RequestBody ProjectRequestDto dto) {
        return ResponseEntity.ok(projectService.create(authentication.getName(), dto));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDto>> my(Authentication authentication) {
        return ResponseEntity.ok(projectService.getMy(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> getById(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(projectService.getById(authentication.getName(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> update(Authentication authentication, @PathVariable Long id, @Valid @RequestBody ProjectRequestDto dto) {
        return ResponseEntity.ok(projectService.update(authentication.getName(), id, dto));
    }

    @PostMapping("/{id}/members/{userId}")
    public ResponseEntity<ProjectResponseDto> addMember(Authentication authentication, @PathVariable Long id, @PathVariable Long userId) {
        return ResponseEntity.ok(projectService.addMember(authentication.getName(), id, userId));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ProjectResponseDto> removeMember(Authentication authentication, @PathVariable Long id, @PathVariable Long userId) {
        return ResponseEntity.ok(projectService.removeMember(authentication.getName(), id, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        projectService.delete(authentication.getName(), id);
        return ResponseEntity.ok().build();
    }
}
