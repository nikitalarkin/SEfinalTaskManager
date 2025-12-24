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
        User author = User.builder().id(2L).email(email).isActive(true).build();

        Project project = Project.builder().id(10L).name("P").build();
        Task task = Task.builder().id(100L).project(project).build();

        Comment comment = Comment.builder()
                .id(5L)
                .author(author)
                .task(task)
                .text("hi")
                .createdAt(Instant.now())
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(author));
        when(commentRepository.findById(5L)).thenReturn(Optional.of(comment));

        commentService.delete(email, 5L);

        verify(commentRepository).delete(comment);
    }

    @Test
    void delete_forbiddenForOtherUser() {
        setAuth("ROLE_USER");

        String email = "user2@test.com";
        User actor = User.builder().id(3L).email(email).isActive(true).build();
        User author = User.builder().id(2L).email("author@test.com").isActive(true).build();

        Comment comment = Comment.builder()
                .id(5L)
                .author(author)
                .text("hi")
                .createdAt(Instant.now())
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(actor));
        when(commentRepository.findById(5L)).thenReturn(Optional.of(comment));

        assertThrows(IllegalArgumentException.class, () -> commentService.delete(email, 5L));
        verify(commentRepository, never()).delete(any());
    }

    private void setAuth(String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("x", "x", List.of(new SimpleGrantedAuthority(role)))
        );
    }
}
