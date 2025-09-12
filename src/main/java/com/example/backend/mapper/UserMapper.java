package com.example.backend.mapper;

import com.example.backend.dto.model.UserDTO;
import com.example.backend.entity.User;
import com.example.backend.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final FileUploadService fileUploadService;

    public UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setUsername(user.getUsername());
        userDTO.setFullName(user.getFullName());
        userDTO.setEnabled(user.getEnabled());
        userDTO.setUserType(user.getUserType());
        userDTO.setLastActive(user.getLastActive());

        if (StringUtils.hasText(user.getUserImage())) {
            userDTO.setUserImage(fileUploadService.generatePresignedGetUrl(user.getUserImage()));
        }
        return userDTO;
    }
}