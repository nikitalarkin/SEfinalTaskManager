package com.example.demo.service.impl;

import com.example.demo.dto.request.ProjectRequestDto;
import com.example.demo.dto.response.ProjectResponseDto;
import com.example.demo.entity.Project;
import com.example.demo.entity.User;
import com.example.demo.mapper.ProjectMapper;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ProjectService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              UserRepository userRepository,
                              ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMapper = projectMapper;
    }

    @Transactional
    @Override
    public ProjectResponseDto create(String email, ProjectRequestDto dto) {
        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Project project = projectMapper.toEntity(dto);
        project.setCreatedBy(creator);

        Set<User> members = new HashSet<>();
        members.add(creator);

        if (dto.memberIds() != null && !dto.memberIds().isEmpty()) {
            List<User> found = userRepository.findAllById(dto.memberIds());
            if (found.size() != dto.memberIds().size()) {
                throw new IllegalArgumentException("Some members not found");
            }
            members.addAll(found);
        }

        project.setMembers(members);

        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProjectResponseDto> getMy(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Project> projects = isAdmin()
                ? projectRepository.findAll()
                : projectRepository.findDistinctByMembersIdOrCreatedById(user.getId(), user.getId());

        return projects.stream().map(projectMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public ProjectResponseDto getById(String email, Long projectId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (!isAdmin() && !canRead(user, project)) {
            throw new IllegalArgumentException("Access denied");
        }

        return projectMapper.toResponse(project);
    }

    @Transactional
    @Override
    public ProjectResponseDto update(String email, Long projectId, ProjectRequestDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (!isAdmin() && !isCreator(user, project)) {
            throw new IllegalArgumentException("Access denied");
        }

        project.setName(dto.name());
        project.setDescription(dto.description());

        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    @Override
    public ProjectResponseDto addMember(String email, Long projectId, Long userId) {
        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (!isAdmin() && !isCreator(actor, project)) {
            throw new IllegalArgumentException("Access denied");
        }

        User member = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        project.getMembers().add(member);

        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    @Override
    public ProjectResponseDto removeMember(String email, Long projectId, Long userId) {
        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (!isAdmin() && !isCreator(actor, project)) {
            throw new IllegalArgumentException("Access denied");
        }

        if (project.getCreatedBy() != null && project.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("Cannot remove creator");
        }

        project.getMembers().removeIf(u -> u.getId().equals(userId));

        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    @Override
    public void delete(String email, Long projectId) {
        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (!isAdmin() && !isCreator(actor, project)) {
            throw new IllegalArgumentException("Access denied");
        }

        projectRepository.delete(project);
    }

    private boolean isCreator(User user, Project project) {
        return project.getCreatedBy() != null && project.getCreatedBy().getId().equals(user.getId());
    }

    private boolean canRead(User user, Project project) {
        if (isCreator(user, project)) return true;
        return project.getMembers() != null && project.getMembers().stream().anyMatch(m -> m.getId().equals(user.getId()));
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
