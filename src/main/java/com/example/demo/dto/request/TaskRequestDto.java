package com.example.demo.dto.request;

import com.example.demo.entity.TaskPriority;
import com.example.demo.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequestDto {
        @NotNull
        private Long projectId;

        @NotBlank
        @Size(max = 300)
        private String title;

        @Size(max = 4000)
        private String description;

        @NotNull
        private TaskStatus status;

        @NotNull
        private TaskPriority priority;

        private LocalDate dueDate;

        private Long assignedToId;
}
