package com.example.backend.controller;

import com.example.backend.dto.model.UserDTO;
import com.example.backend.dto.request.admin.AssignRoleRequest;
import com.example.backend.service.UserService;
import com.example.backend.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        return ResponseEntity.ok(userService.getUser());
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateCurrentUser(@RequestBody UserDTO userDTO) {
        userService.updateUser(userDTO , userDTO.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/roles")
    public ResponseEntity<Void> addRoleToCurrentUser(@RequestBody AssignRoleRequest request) throws BadRequestException {
        UUID currentUserId = getCurrentUserId();
        roleService.assignRole(currentUserId, request.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private UUID getCurrentUserId() {
        UserDTO currentUser = userService.getUser();
        return currentUser.getId();
    }
}