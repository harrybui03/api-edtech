package com.example.backend.dto.model;

import com.example.backend.constant.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {

    private UUID id;

    private String email;

    private String username;

    private String fullName;

    private String userImage;

    private Boolean enabled;

    private UserType userType;

    private OffsetDateTime lastActive;

    private Set<UserRoleDTO> roles;
}
