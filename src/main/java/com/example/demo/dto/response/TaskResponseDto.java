package com.example.demo.dto.response;

import com.example.demo.entity.TaskPriority;
import com.example.demo.entity.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponseDto(
        Long id,
        Long projectId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Long createdById,
        Long assignedToId,
        Instant createdAt,
        Instant updatedAt
) {
}
