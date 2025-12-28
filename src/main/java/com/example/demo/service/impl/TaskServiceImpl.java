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
        java.util.Optional<User> creatorOpt = userRepository.findByEmail(email);
        if (creatorOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User creator = creatorOpt.get();

        java.util.Optional<Project> projectOpt = projectRepository.findById(dto.getProjectId());
        if (projectOpt.isEmpty()) {
            throw new IllegalArgumentException("Project not found");
        }
        Project project = projectOpt.get();

        if (!isAdmin() && !canReadProject(creator, project)) {
            throw new IllegalArgumentException("Access denied");
        }

        Task task = taskMapper.toEntity(dto);
        task.setProject(project);
        task.setCreatedBy(creator);

        if (dto.getAssignedToId() != null) {
            java.util.Optional<User> assignedOpt = userRepository.findById(dto.getAssignedToId());
            if (assignedOpt.isEmpty()) {
                throw new IllegalArgumentException("Assigned user not found");
            }
            User assigned = assignedOpt.get();
            task.setAssignedTo(assigned);
        }

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    @Override
    public TaskResponseDto getById(String email, Long id) {
        java.util.Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userOpt.get();

        java.util.Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task not found");
        }
        Task task = taskOpt.get();

        if (!isAdmin() && !canReadProject(user, task.getProject())) {
            throw new IllegalArgumentException("Access denied");
        }

        return taskMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TaskResponseDto> getAll(String email, Long projectId, TaskStatus status, Long assignedToId) {
        java.util.Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userOpt.get();

        List<Task> tasks;

        if (projectId != null) {
            java.util.Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                throw new IllegalArgumentException("Project not found");
            }
            Project project = projectOpt.get();

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
                List<Project> projects = projectRepository.findDistinctByMembersIdOrCreatedById(user.getId(),
                        user.getId());
                List<Long> ids = new java.util.ArrayList<>();
                for (Project project : projects) {
                    ids.add(project.getId());
                }
                tasks = ids.isEmpty() ? List.of() : taskRepository.findAllByProjectIdIn(ids);
            }
        }

        List<Task> filteredTasks = new java.util.ArrayList<>();
        for (Task t : tasks) {
            boolean matches = true;
            if (assignedToId != null) {
                if (t.getAssignedTo() == null || !t.getAssignedTo().getId().equals(assignedToId)) {
                    matches = false;
                }
            }
            if (status != null && projectId == null) {
                if (t.getStatus() != status) {
                    matches = false;
                }
            }
            if (matches) {
                filteredTasks.add(t);
            }
        }

        List<TaskResponseDto> response = new java.util.ArrayList<>();
        for (Task t : filteredTasks) {
            response.add(taskMapper.toResponse(t));
        }

        return response;
    }

    @Transactional
    @Override
    public TaskResponseDto update(String email, Long id, TaskRequestDto dto) {
        java.util.Optional<User> actorOpt = userRepository.findByEmail(email);
        if (actorOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User actor = actorOpt.get();

        java.util.Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task not found");
        }
        Task task = taskOpt.get();

        if (!isAdmin() && !canManage(actor, task)) {
            throw new IllegalArgumentException("Access denied");
        }

        if (dto.getTitle() != null)
            task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        if (dto.getStatus() != null)
            task.setStatus(dto.getStatus());
        if (dto.getPriority() != null)
            task.setPriority(dto.getPriority());
        task.setDueDate(dto.getDueDate());

        if (dto.getAssignedToId() != null) {
            java.util.Optional<User> assignedOpt = userRepository.findById(dto.getAssignedToId());
            if (assignedOpt.isEmpty()) {
                throw new IllegalArgumentException("Assigned user not found");
            }
            User assigned = assignedOpt.get();
            task.setAssignedTo(assigned);
        }

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    @Override
    public TaskResponseDto changeStatus(String email, Long id, TaskStatus status) {
        java.util.Optional<User> actorOpt = userRepository.findByEmail(email);
        if (actorOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User actor = actorOpt.get();

        java.util.Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task not found");
        }
        Task task = taskOpt.get();

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
        java.util.Optional<User> actorOpt = userRepository.findByEmail(email);
        if (actorOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User actor = actorOpt.get();

        java.util.Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task not found");
        }
        Task task = taskOpt.get();

        if (!isAdmin() && !canManage(actor, task)) {
            throw new IllegalArgumentException("Access denied");
        }

        taskRepository.delete(task);
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

    private boolean canManage(User actor, Task task) {
        Project project = task.getProject();
        if (project == null)
            return false;
        if (project.getCreatedBy() != null && project.getCreatedBy().getId().equals(actor.getId()))
            return true;
        return hasRole("ROLE_MANAGER");
    }

    private boolean isAssignee(User actor, Task task) {
        return task.getAssignedTo() != null && task.getAssignedTo().getId().equals(actor.getId());
    }

    private boolean hasRole(String role) {
        for (org.springframework.security.core.GrantedAuthority a : SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities()) {
            if (a.getAuthority().equals(role)) {
                return true;
            }
        }
        return false;
    }
}
