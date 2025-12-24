package com.example.demo.mapper;

import com.example.demo.dto.request.ProjectRequestDto;
import com.example.demo.dto.response.ProjectResponseDto;
import com.example.demo.entity.Project;
import com.example.demo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "members", ignore = true)
    Project toEntity(ProjectRequestDto dto);

    @Mapping(target = "createdById", expression = "java(userId(project.getCreatedBy()))")
    @Mapping(target = "memberIds", expression = "java(userIds(project.getMembers()))")
    ProjectResponseDto toResponse(Project project);

    default Long userId(User user) {
        return user == null ? null : user.getId();
    }

    default Set<Long> userIds(Set<User> users) {
        if (users == null) return Set.of();
        return users.stream().map(User::getId).collect(Collectors.toSet());
    }
}
