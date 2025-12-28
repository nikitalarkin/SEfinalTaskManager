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
        java.util.Optional<User> creatorOpt = userRepository.findByEmail(email);
        if (creatorOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User creator = creatorOpt.get();

        Project project = projectMapper.toEntity(dto);
        project.setCreatedBy(creator);

        Set<User> members = new HashSet<>();
        members.add(creator);

        if (dto.getMemberIds() != null && !dto.getMemberIds().isEmpty()) {
            List<User> found = userRepository.findAllById(dto.getMemberIds());
            if (found.size() != dto.getMemberIds().size()) {
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
        java.util.Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userOpt.get();

        List<Project> projects = isAdmin()
                ? projectRepository.findAll()
                : projectRepository.findDistinctByMembersIdOrCreatedById(user.getId(), user.getId());

        List<ProjectResponseDto> response = new java.util.ArrayList<>();
        for (Project project : projects) {
            response.add(projectMapper.toResponse(project));
        }
        return response;
    }

    @Transactional(readOnly = true)
    @Override
    public ProjectResponseDto getById(String email, Long projectId) {
        java.util.Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userOpt.get();

        java.util.Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw new IllegalArgumentException("Project not found");
        }
        Project project = projectOpt.get();

        if (!isAdmin() && !canRead(user, project)) {
            throw new IllegalArgumentException("Access denied");
        }

        return projectMapper.toResponse(project);
    }

    @Transactional
    @Override
    public ProjectResponseDto update(String email, Long projectId, ProjectRequestDto dto) {
        java.util.Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userOpt.get();

        java.util.Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw new IllegalArgumentException("Project not found");
        }
        Project project = projectOpt.get();

        if (!isAdmin() && !isCreator(user, project)) {
            throw new IllegalArgumentException("Access denied");
        }

        project.setName(dto.getName());
        project.setDescription(dto.getDescription());

        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    @Override
    public ProjectResponseDto addMember(String email, Long projectId, Long userId) {
        java.util.Optional<User> actorOpt = userRepository.findByEmail(email);
        if (actorOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User actor = actorOpt.get();

        java.util.Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw new IllegalArgumentException("Project not found");
        }
        Project project = projectOpt.get();

        if (!isAdmin() && !isCreator(actor, project)) {
            throw new IllegalArgumentException("Access denied");
        }

        java.util.Optional<User> memberOpt = userRepository.findById(userId);
        if (memberOpt.isEmpty()) {
            throw new IllegalArgumentException("Member not found");
        }
        User member = memberOpt.get();

        project.getMembers().add(member);

        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    @Override
    public ProjectResponseDto removeMember(String email, Long projectId, Long userId) {
        java.util.Optional<User> actorOpt = userRepository.findByEmail(email);
        if (actorOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User actor = actorOpt.get();

        java.util.Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw new IllegalArgumentException("Project not found");
        }
        Project project = projectOpt.get();

        if (!isAdmin() && !isCreator(actor, project)) {
            throw new IllegalArgumentException("Access denied");
        }

        if (project.getCreatedBy() != null && project.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("Cannot remove creator");
        }

        java.util.Iterator<User> iterator = project.getMembers().iterator();
        while (iterator.hasNext()) {
            User u = iterator.next();
            if (u.getId().equals(userId)) {
                iterator.remove();
            }
        }

        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    @Override
    public void delete(String email, Long projectId) {
        java.util.Optional<User> actorOpt = userRepository.findByEmail(email);
        if (actorOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User actor = actorOpt.get();

        java.util.Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw new IllegalArgumentException("Project not found");
        }
        Project project = projectOpt.get();

        if (!isAdmin() && !isCreator(actor, project)) {
            throw new IllegalArgumentException("Access denied");
        }

        projectRepository.delete(project);
    }

    private boolean isCreator(User user, Project project) {
        return project.getCreatedBy() != null && project.getCreatedBy().getId().equals(user.getId());
    }

    private boolean canRead(User user, Project project) {
        if (isCreator(user, project))
            return true;
        if (project.getMembers() != null) {
            for (User m : project.getMembers()) {
                if (m.getId().equals(user.getId())) {
                    return true;
                }
            }
        }
        return false;
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
}
