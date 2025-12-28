package com.example.demo.service.impl;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Project;
import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.mapper.CommentMapper;
import com.example.demo.repository.CommentRepository;
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
class CommentServiceImplTest {

    @Mock
    CommentRepository commentRepository;

    @Mock
    TaskRepository taskRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CommentMapper commentMapper;

    @InjectMocks
    CommentServiceImpl commentService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void delete_allowsAuthor() {
        setAuth("ROLE_USER");

        String email = "user@test.com";
        User author = new User();
        author.setId(2L);
        author.setEmail(email);
        author.setActive(true);

        Project project = new Project();
        project.setId(10L);
        project.setName("P");

        Task task = new Task();
        task.setId(100L);
        task.setProject(project);

        Comment comment = new Comment();
        comment.setId(5L);
        comment.setAuthor(author);
        comment.setTask(task);
        comment.setText("hi");
        comment.setCreatedAt(Instant.now());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(author));
        when(commentRepository.findById(5L)).thenReturn(Optional.of(comment));

        commentService.delete(email, 5L);

        verify(commentRepository).delete(comment);
    }

    @Test
    void delete_forbiddenForOtherUser() {
        setAuth("ROLE_USER");

        String email = "user2@test.com";
        User actor = new User();
        actor.setId(3L);
        actor.setEmail(email);
        actor.setActive(true);
        User author = new User();
        author.setId(2L);
        author.setEmail("author@test.com");
        author.setActive(true);

        Comment comment = new Comment();
        comment.setId(5L);
        comment.setAuthor(author);
        comment.setText("hi");
        comment.setCreatedAt(Instant.now());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(actor));
        when(commentRepository.findById(5L)).thenReturn(Optional.of(comment));

        assertThrows(IllegalArgumentException.class, () -> commentService.delete(email, 5L));
        verify(commentRepository, never()).delete(any());
    }

    private void setAuth(String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("x", "x", List.of(new SimpleGrantedAuthority(role))));
    }
}
