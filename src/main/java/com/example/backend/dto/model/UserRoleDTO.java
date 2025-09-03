package com.example.backend.dto.model;

import com.example.backend.constant.UserRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRoleDTO {

    private UUID id;

    private UUID userId;

    private UserRoleEnum role;
}
