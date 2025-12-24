package com.example.demo.mapper;

import com.example.demo.dto.request.RoleRequestDto;
import com.example.demo.dto.response.RoleResponseDto;
import com.example.demo.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    Role toEntity(RoleRequestDto dto);
    RoleResponseDto toResponse(Role role);
}
