package com.example.demo.service;

import com.example.demo.dto.request.CommentRequestDto;
import com.example.demo.dto.response.CommentResponseDto;

import java.util.List;

public interface CommentService {
    CommentResponseDto add(String email, CommentRequestDto dto);
    List<CommentResponseDto> getByTask(String email, Long taskId);
    void delete(String email, Long commentId);
}
