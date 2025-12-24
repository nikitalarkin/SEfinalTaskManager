package com.example.demo.controller;

import com.example.demo.dto.request.CommentRequestDto;
import com.example.demo.dto.response.CommentResponseDto;
import com.example.demo.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/comments")
    public ResponseEntity<CommentResponseDto> add(Authentication authentication, @Valid @RequestBody CommentRequestDto dto) {
        return ResponseEntity.ok(commentService.add(authentication.getName(), dto));
    }

    @GetMapping("/tasks/{taskId}/comments")
    public ResponseEntity<List<CommentResponseDto>> list(Authentication authentication, @PathVariable Long taskId) {
        return ResponseEntity.ok(commentService.getByTask(authentication.getName(), taskId));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        commentService.delete(authentication.getName(), id);
        return ResponseEntity.ok().build();
    }
}
