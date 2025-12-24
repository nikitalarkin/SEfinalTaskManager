package com.example.demo.service.impl;

import com.example.demo.dto.response.TaskResponseDto;
import com.example.demo.entity.Project;
import com.example.demo.entity.Task;
import com.example.demo.entity.TaskPriority;
import com.example.demo.entity.TaskStatus;
import com.example.demo.entity.User;
import com.example.demo.mapper.TaskMapper;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    TaskRepository taskRepository;

    @Mock
    ProjectRepository projectRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    TaskMapper taskMapper;

    @InjectMocks
    TaskServiceImpl taskService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void changeStatus_allowsAssignee() {
        setAuth("ROLE_USER");

        String email = "user@test.com";
        User actor = User.builder().id(2L).email(email).isActive(true).build();

        User creator = User.builder().id(1L).email("creator@test.com").isActive(true).build();
        Project project = Project.builder().id(10L).name("P").createdBy(creator).build();

        Task task = Task.builder()
                .id(100L)
                .project(project)
                .title("T")
                .status(TaskStatus.NEW)
                .priority(TaskPriority.MEDIUM)
                .createdBy(creator)
                .assignedTo(actor)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(actor));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toResponse(any(Task.class))).thenReturn(new TaskResponseDto(
                100L, 10L, "T", null, TaskStatus.DONE, TaskPriority.MEDIUM, null, 1L, 2L, task.getCreatedAt(), task.getUpdatedAt()
        ));

        TaskResponseDto resp = taskService.changeStatus(email, 100L, TaskStatus.DONE);

        assertEquals(TaskStatus.DONE, resp.status());
        assertEquals(TaskStatus.DONE, task.getStatus());
        verify(taskRepository).save(task);
    }

    @Test
    void getAll_nonAdmin_returnsOnlyAccessibleProjects() {
        setAuth("ROLE_USER");

        String email = "user@test.com";
        User user = User.builder().id(2L).email(email).isActive(true).build();

        Project p1 = Project.builder().id(10L).name("P1").build();
        Project p2 = Project.builder().id(20L).name("P2").build();

        Task t1 = Task.builder().id(1L).project(p1).title("A").status(TaskStatus.NEW).priority(TaskPriority.MEDIUM).build();
        Task t2 = Task.builder().id(2L).project(p2).title("B").status(TaskStatus.NEW).priority(TaskPriority.MEDIUM).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(projectRepository.findDistinctByMembersIdOrCreatedById(2L, 2L)).thenReturn(List.of(p1, p2));
        when(taskRepository.findAllByProjectIdIn(List.of(10L, 20L))).thenReturn(List.of(t1, t2));
        when(taskMapper.toResponse(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            return new TaskResponseDto(t.getId(), t.getProject().getId(), t.getTitle(), t.getDescription(), t.getStatus(), t.getPriority(), t.getDueDate(), null, null, t.getCreatedAt(), t.getUpdatedAt());
        });

        List<TaskResponseDto> res = taskService.getAll(email, null, null, null);

        assertEquals(2, res.size());
    }

    private void setAuth(String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("x", "x", List.of(new SimpleGrantedAuthority(role)))
        );
    }
}
