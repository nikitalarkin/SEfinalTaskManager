package com.example.demo.service.impl;

import com.example.demo.dto.request.CommentRequestDto;
import com.example.demo.dto.response.CommentResponseDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Project;
import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.mapper.CommentMapper;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CommentService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    public CommentServiceImpl(CommentRepository commentRepository,
                              TaskRepository taskRepository,
                              UserRepository userRepository,
                              CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.commentMapper = commentMapper;
    }

    @Transactional
    @Override
    public CommentResponseDto add(String email, CommentRequestDto dto) {
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Task task = taskRepository.findById(dto.taskId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!isAdmin() && !canReadProject(author, task.getProject())) {
            throw new IllegalArgumentException("Access denied");
        }

        Comment comment = commentMapper.toEntity(dto);
        comment.setAuthor(author);
        comment.setTask(task);

        return commentMapper.toResponse(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentResponseDto> getByTask(String email, Long taskId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!isAdmin() && !canReadProject(user, task.getProject())) {
            throw new IllegalArgumentException("Access denied");
        }

        return commentRepository.findAllByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(commentMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(String email, Long commentId) {
        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        boolean canDelete = isAdmin() || (comment.getAuthor() != null && comment.getAuthor().getId().equals(actor.getId()));
        if (!canDelete) {
            throw new IllegalArgumentException("Access denied");
        }

        commentRepository.delete(comment);
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean canReadProject(User user, Project project) {
        if (project == null) return false;
        if (project.getCreatedBy() != null && project.getCreatedBy().getId().equals(user.getId())) return true;
        Set<User> members = project.getMembers();
        return members != null && members.stream().anyMatch(m -> m.getId().equals(user.getId()));
    }
}
