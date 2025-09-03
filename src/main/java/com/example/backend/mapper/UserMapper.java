package com.example.backend.mapper;

import com.example.backend.dto.model.UserDTO;
import com.example.backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final UserRoleMapper userRoleMapper;

    public UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setUsername(user.getUsername());
        userDTO.setFullName(user.getFullName());
        userDTO.setUserImage(user.getUserImage());
        userDTO.setEnabled(user.getEnabled());
        userDTO.setUserType(user.getUserType());
        userDTO.setLastActive(user.getLastActive());

        if (user.getRoles() != null) {
            userDTO.setRoles(user.getRoles().stream()
                    .map(userRoleMapper::toUserRoleDTO)
                    .collect(Collectors.toSet()));
        }

        return userDTO;
    }
}