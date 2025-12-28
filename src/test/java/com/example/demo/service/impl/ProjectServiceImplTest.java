package com.example.demo.service.impl;

import com.example.demo.dto.request.ProjectRequestDto;
import com.example.demo.dto.response.ProjectResponseDto;
import com.example.demo.entity.Project;
import com.example.demo.entity.User;
import com.example.demo.mapper.ProjectMapper;
import com.example.demo.repository.ProjectRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    ProjectRepository projectRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ProjectMapper projectMapper;

    @InjectMocks
    ProjectServiceImpl projectService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_success_addsCreatorToMembers() {
        setAuth("ROLE_MANAGER");

        String email = "manager@test.com";
        User creator = new User();
        creator.setId(1L);
        creator.setEmail(email);
        creator.setActive(true);

        ProjectRequestDto dto = new ProjectRequestDto("Project A", "Desc", Set.of());
        Project entity = new Project();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(creator));
        when(projectMapper.toEntity(dto)).thenReturn(entity);
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));
        when(projectMapper.toResponse(any(Project.class))).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            return new ProjectResponseDto(10L, p.getName(), p.getDescription(), p.getCreatedBy().getId(),
                    p.getCreatedAt(),
                    p.getMembers().stream().map(User::getId).collect(java.util.stream.Collectors.toSet()));
        });

        ProjectResponseDto resp = projectService.create(email, dto);

        assertEquals(dto.getName(), resp.getName());
        assertNotNull(resp.getCreatedById());
        assertTrue(resp.getMemberIds().contains(creator.getId()));
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void getMy_admin_returnsAll() {
        setAuth("ROLE_ADMIN");

        String email = "admin@test.com";
        User admin = new User();
        admin.setId(99L);
        admin.setEmail(email);
        admin.setActive(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(admin));
        Project p1 = new Project();
        p1.setId(1L);
        p1.setName("P1");
        when(projectRepository.findAll()).thenReturn(List.of(p1));
        when(projectMapper.toResponse(any(Project.class)))
                .thenReturn(new ProjectResponseDto(1L, "P1", null, null, null, Set.of()));

        List<ProjectResponseDto> res = projectService.getMy(email);
        assertEquals(1, res.size());
    }

    private void setAuth(String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("x", "x", List.of(new SimpleGrantedAuthority(role))));
    }
}
