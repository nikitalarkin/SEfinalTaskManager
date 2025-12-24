package com.example.demo.dto.request;

import com.example.demo.entity.TaskPriority;
import com.example.demo.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequestDto(
        @NotNull
        Long projectId,

        @NotBlank
        @Size(max = 300)
        String title,

        @Size(max = 4000)
        String description,

        @NotNull
        TaskStatus status,

        @NotNull
        TaskPriority priority,

        LocalDate dueDate,

        Long assignedToId
) {
}
