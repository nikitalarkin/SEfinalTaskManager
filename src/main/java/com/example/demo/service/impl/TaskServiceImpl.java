package com.example.demo.service.impl;

import com.example.demo.dto.request.TaskRequestDto;
import com.example.demo.dto.response.TaskResponseDto;
import com.example.demo.entity.Project;
import com.example.demo.entity.Task;
import com.example.demo.entity.TaskStatus;
import com.example.demo.entity.User;
import com.example.demo.mapper.TaskMapper;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.TaskService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public TaskServiceImpl(TaskRepository taskRepository,
                           ProjectRepository projectRepository,
                           UserRepository userRepository,
                           TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
    }

    @Transactional
    @Override
    public TaskResponseDto create(String email, TaskRequestDto dto) {
        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Project project = projectRepository.findById(dto.projectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (!isAdmin() && !canReadProject(creator, project)) {
            throw new IllegalArgumentException("Access denied");
        }

        Task task = taskMapper.toEntity(dto);
        task.setProject(project);
        task.setCreatedBy(creator);

        if (dto.assignedToId() != null) {
            User assigned = userRepository.findById(dto.assignedToId())
                    .orElseThrow(() -> new IllegalArgumentException("Assigned user not found"));
            task.setAssignedTo(assigned);
        }

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    @Override
    public TaskResponseDto getById(String email, Long id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!isAdmin() && !canReadProject(user, task.getProject())) {
            throw new IllegalArgumentException("Access denied");
        }

        return taskMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TaskResponseDto> getAll(String email, Long projectId, TaskStatus status, Long assignedToId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Task> tasks;

        if (projectId != null) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new IllegalArgumentException("Project not found"));

            if (!isAdmin() && !canReadProject(user, project)) {
                throw new IllegalArgumentException("Access denied");
            }

            if (status != null) {
                tasks = taskRepository.findAllByProjectIdAndStatus(projectId, status);
            } else {
                tasks = taskRepository.findAllByProjectId(projectId);
            }
        } else {
            if (isAdmin()) {
                tasks = taskRepository.findAll();
            } else {
                List<Project> projects = projectRepository.findDistinctByMembersIdOrCreatedById(user.getId(), user.getId());
                List<Long> ids = projects.stream().map(Project::getId).toList();
                tasks = ids.isEmpty() ? List.of() : taskRepository.findAllByProjectIdIn(ids);
            }
        }

        if (assignedToId != null) {
            tasks = tasks.stream()
                    .filter(t -> t.getAssignedTo() != null && t.getAssignedTo().getId().equals(assignedToId))
                    .toList();
        }

        if (status != null && projectId == null) {
            tasks = tasks.stream()
                    .filter(t -> t.getStatus() == status)
                    .toList();
        }

        return tasks.stream().map(taskMapper::toResponse).toList();
    }

    @Transactional
    @Override
    public TaskResponseDto update(String email, Long id, TaskRequestDto dto) {
        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!isAdmin() && !canManage(actor, task)) {
            throw new IllegalArgumentException("Access denied");
        }

        if (dto.title() != null) task.setTitle(dto.title());
        task.setDescription(dto.description());
        if (dto.status() != null) task.setStatus(dto.status());
        if (dto.priority() != null) task.setPriority(dto.priority());
        task.setDueDate(dto.dueDate());

        if (dto.assignedToId() != null) {
            User assigned = userRepository.findById(dto.assignedToId())
                    .orElseThrow(() -> new IllegalArgumentException("Assigned user not found"));
            task.setAssignedTo(assigned);
        }

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    @Override
    public TaskResponseDto changeStatus(String email, Long id, TaskStatus status) {
        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        boolean canChange = isAdmin() || canManage(actor, task) || isAssignee(actor, task);
        if (!canChange) {
            throw new IllegalArgumentException("Access denied");
        }

        task.setStatus(status);
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    @Override
    public void delete(String email, Long id) {
        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!isAdmin() && !canManage(actor, task)) {
            throw new IllegalArgumentException("Access denied");
        }

        taskRepository.delete(task);
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

    private boolean canManage(User actor, Task task) {
        Project project = task.getProject();
        if (project == null) return false;
        if (project.getCreatedBy() != null && project.getCreatedBy().getId().equals(actor.getId())) return true;
        return hasRole("ROLE_MANAGER");
    }

    private boolean isAssignee(User actor, Task task) {
        return task.getAssignedTo() != null && task.getAssignedTo().getId().equals(actor.getId());
    }

    private boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
}
