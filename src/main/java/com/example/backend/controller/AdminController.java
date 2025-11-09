package com.example.backend.controller;

import com.example.backend.dto.model.UserDTO;
import com.example.backend.dto.model.UserRoleDTO;
import com.example.backend.dto.request.admin.AssignRoleRequest;
import com.example.backend.dto.response.pagination.PaginationResponse;
import com.example.backend.service.RoleService;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    @GetMapping("/users")
    public ResponseEntity<PaginationResponse<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getUsers(page, size));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<Void> updateUser(@PathVariable UUID userId, @RequestBody UserDTO userDTO) {
        userService.updateUser(userDTO , userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{userId}/roles")
    public ResponseEntity<List<UserRoleDTO>> getUserRoles(@PathVariable UUID userId) {
        return ResponseEntity.ok(roleService.getRoles(userId));
    }

    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<Void> assignRole(@PathVariable UUID userId, @RequestBody AssignRoleRequest request) throws BadRequestException {
        roleService.assignRole(userId, request.getRole());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{userId}/roles")
    public ResponseEntity<Void> removeRole(@PathVariable UUID userId, @RequestBody AssignRoleRequest request) {
        roleService.removeRole(userId, request.getRole());
        return ResponseEntity.ok().build();
    }
}