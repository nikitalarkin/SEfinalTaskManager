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
        java.util.Optional<User> authorOpt = userRepository.findByEmail(email);
        if (authorOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User author = authorOpt.get();

        java.util.Optional<Task> taskOpt = taskRepository.findById(dto.getTaskId());
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task not found");
        }
        Task task = taskOpt.get();

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
        java.util.Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userOpt.get();

        java.util.Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task not found");
        }
        Task task = taskOpt.get();

        if (!isAdmin() && !canReadProject(user, task.getProject())) {
            throw new IllegalArgumentException("Access denied");
        }

        List<com.example.demo.entity.Comment> comments = commentRepository.findAllByTaskIdOrderByCreatedAtAsc(taskId);
        List<CommentResponseDto> response = new java.util.ArrayList<>();
        for (com.example.demo.entity.Comment c : comments) {
            response.add(commentMapper.toResponse(c));
        }
        return response;
    }

    @Transactional
    @Override
    public void delete(String email, Long commentId) {
        java.util.Optional<User> actorOpt = userRepository.findByEmail(email);
        if (actorOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User actor = actorOpt.get();

        java.util.Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            throw new IllegalArgumentException("Comment not found");
        }
        Comment comment = commentOpt.get();

        boolean canDelete = isAdmin()
                || (comment.getAuthor() != null && comment.getAuthor().getId().equals(actor.getId()));
        if (!canDelete) {
            throw new IllegalArgumentException("Access denied");
        }

        commentRepository.delete(comment);
    }

    private boolean isAdmin() {
        for (org.springframework.security.core.GrantedAuthority a : SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities()) {
            if (a.getAuthority().equals("ROLE_ADMIN")) {
                return true;
            }
        }
        return false;
    }

    private boolean canReadProject(User user, Project project) {
        if (project == null)
            return false;
        if (project.getCreatedBy() != null && project.getCreatedBy().getId().equals(user.getId()))
            return true;
        Set<User> members = project.getMembers();
        if (members != null) {
            for (User m : members) {
                if (m.getId().equals(user.getId())) {
                    return true;
                }
            }
        }
        return false;
    }
}
