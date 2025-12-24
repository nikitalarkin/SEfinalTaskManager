package com.example.demo.controller;

import com.example.demo.dto.request.TaskRequestDto;
import com.example.demo.dto.response.TaskResponseDto;
import com.example.demo.entity.TaskStatus;
import com.example.demo.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<TaskResponseDto> create(Authentication authentication, @Valid @RequestBody TaskRequestDto dto) {
        return ResponseEntity.ok(taskService.create(authentication.getName(), dto));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> list(Authentication authentication,
                                                      @RequestParam(required = false) Long projectId,
                                                      @RequestParam(required = false) TaskStatus status,
                                                      @RequestParam(required = false) Long assignedToId) {
        return ResponseEntity.ok(taskService.getAll(authentication.getName(), projectId, status, assignedToId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getById(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(taskService.getById(authentication.getName(), id));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> update(Authentication authentication,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody TaskRequestDto dto) {
        return ResponseEntity.ok(taskService.update(authentication.getName(), id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponseDto> changeStatus(Authentication authentication,
                                                        @PathVariable Long id,
                                                        @RequestParam TaskStatus status) {
        return ResponseEntity.ok(taskService.changeStatus(authentication.getName(), id, status));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        taskService.delete(authentication.getName(), id);
        return ResponseEntity.ok().build();
    }
}
