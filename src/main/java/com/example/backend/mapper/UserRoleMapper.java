package com.example.backend.mapper;

import com.example.backend.dto.model.UserRoleDTO;
import com.example.backend.entity.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserRoleMapper {

    public UserRoleDTO toUserRoleDTO(UserRole userRole) {
        if (userRole == null) {
            return null;
        }
        return UserRoleDTO.builder()
                .id(userRole.getId())
                .userId(userRole.getUser().getId())
                .role(userRole.getRole())
                .build();
    }
}