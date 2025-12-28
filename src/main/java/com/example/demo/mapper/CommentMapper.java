package com.example.demo.mapper;

import com.example.demo.dto.request.CommentRequestDto;
import com.example.demo.dto.response.CommentResponseDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Comment toEntity(CommentRequestDto dto);

    @Mapping(target = "taskId", source = "task")
    @Mapping(target = "authorId", source = "author")
    CommentResponseDto toResponse(Comment comment);

    default Long userId(User user) {
        return user == null ? null : user.getId();
    }

    default Long taskId(Task task) {
        return task == null ? null : task.getId();
    }
}
