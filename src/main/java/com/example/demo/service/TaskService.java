package com.example.demo.service;

import com.example.demo.dto.request.TaskRequestDto;
import com.example.demo.dto.response.TaskResponseDto;
import com.example.demo.entity.TaskStatus;

import java.util.List;

public interface TaskService {
    TaskResponseDto create(String email, TaskRequestDto dto);
    TaskResponseDto getById(String email, Long id);
    List<TaskResponseDto> getAll(String email, Long projectId, TaskStatus status, Long assignedToId);
    TaskResponseDto update(String email, Long id, TaskRequestDto dto);
    TaskResponseDto changeStatus(String email, Long id, TaskStatus status);
    void delete(String email, Long id);
}
