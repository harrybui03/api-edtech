package com.example.backend.dto.request.admin;

import com.example.backend.constant.UserRoleEnum;
import lombok.Data;

@Data
public class AssignRoleRequest {
    private UserRoleEnum role;
}