package com.example.demo.mapper;

import com.example.demo.dto.request.TaskRequestDto;
import com.example.demo.dto.response.TaskResponseDto;
import com.example.demo.entity.Project;
import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Task toEntity(TaskRequestDto dto);

    @Mapping(target = "projectId", expression = "java(projectId(task.getProject()))")
    @Mapping(target = "createdById", expression = "java(userId(task.getCreatedBy()))")
    @Mapping(target = "assignedToId", expression = "java(userId(task.getAssignedTo()))")
    TaskResponseDto toResponse(Task task);

    default Long userId(User user) {
        return user == null ? null : user.getId();
    }

    default Long projectId(Project project) {
        return project == null ? null : project.getId();
    }
}
