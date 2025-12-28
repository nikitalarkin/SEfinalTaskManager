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
        User actor = new User();
        actor.setId(2L);
        actor.setEmail(email);
        actor.setActive(true);

        User creator = new User();
        creator.setId(1L);
        creator.setEmail("creator@test.com");
        creator.setActive(true);

        Project project = new Project();
        project.setId(10L);
        project.setName("P");
        project.setCreatedBy(creator);

        Task task = new Task();
        task.setId(100L);
        task.setProject(project);
        task.setTitle("T");
        task.setStatus(TaskStatus.NEW);
        task.setPriority(TaskPriority.MEDIUM);
        task.setCreatedBy(creator);
        task.setAssignedTo(actor);
        task.setCreatedAt(Instant.now());
        task.setUpdatedAt(Instant.now());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(actor));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toResponse(any(Task.class))).thenReturn(new TaskResponseDto(
                100L, 10L, "T", null, TaskStatus.DONE, TaskPriority.MEDIUM, null, 1L, 2L, task.getCreatedAt(),
                task.getUpdatedAt()));

        TaskResponseDto resp = taskService.changeStatus(email, 100L, TaskStatus.DONE);

        assertEquals(TaskStatus.DONE, resp.getStatus());
        assertEquals(TaskStatus.DONE, task.getStatus());
        verify(taskRepository).save(task);
    }

    @Test
    void getAll_nonAdmin_returnsOnlyAccessibleProjects() {
        setAuth("ROLE_USER");

        String email = "user@test.com";
        User user = new User();
        user.setId(2L);
        user.setEmail(email);
        user.setActive(true);

        Project p1 = new Project();
        p1.setId(10L);
        p1.setName("P1");
        Project p2 = new Project();
        p2.setId(20L);
        p2.setName("P2");

        Task t1 = new Task();
        t1.setId(1L);
        t1.setProject(p1);
        t1.setTitle("A");
        t1.setStatus(TaskStatus.NEW);
        t1.setPriority(TaskPriority.MEDIUM);
        Task t2 = new Task();
        t2.setId(2L);
        t2.setProject(p2);
        t2.setTitle("B");
        t2.setStatus(TaskStatus.NEW);
        t2.setPriority(TaskPriority.MEDIUM);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(projectRepository.findDistinctByMembersIdOrCreatedById(2L, 2L)).thenReturn(List.of(p1, p2));
        when(taskRepository.findAllByProjectIdIn(List.of(10L, 20L))).thenReturn(List.of(t1, t2));
        when(taskMapper.toResponse(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            return new TaskResponseDto(t.getId(), t.getProject().getId(), t.getTitle(), t.getDescription(),
                    t.getStatus(), t.getPriority(), t.getDueDate(), null, null, t.getCreatedAt(), t.getUpdatedAt());
        });

        List<TaskResponseDto> res = taskService.getAll(email, null, null, null);

        assertEquals(2, res.size());
    }

    private void setAuth(String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("x", "x", List.of(new SimpleGrantedAuthority(role))));
    }
}
