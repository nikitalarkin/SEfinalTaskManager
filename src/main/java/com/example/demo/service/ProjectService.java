package com.example.demo.service;

import com.example.demo.dto.request.ProjectRequestDto;
import com.example.demo.dto.response.ProjectResponseDto;

import java.util.List;

public interface ProjectService {
    ProjectResponseDto create(String email, ProjectRequestDto dto);
    List<ProjectResponseDto> getMy(String email);
    ProjectResponseDto getById(String email, Long projectId);
    ProjectResponseDto update(String email, Long projectId, ProjectRequestDto dto);
    ProjectResponseDto addMember(String email, Long projectId, Long userId);
    ProjectResponseDto removeMember(String email, Long projectId, Long userId);
    void delete(String email, Long projectId);
}
