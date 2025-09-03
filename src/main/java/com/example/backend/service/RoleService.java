package com.example.backend.service;

import com.example.backend.constant.UserRoleEnum;
import com.example.backend.dto.model.UserRoleDTO;
import com.example.backend.entity.User;
import com.example.backend.entity.UserRole;
import com.example.backend.excecption.InvalidRequestDataException;
import com.example.backend.excecption.ResourceNotFoundException;
import com.example.backend.mapper.UserRoleMapper;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.UserRoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final UserRoleMapper userRoleMapper;

    public List<UserRoleDTO> getRoles(UUID userId){
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return userRoleRepository.findAllByUserId(userId).stream()
                .map(userRoleMapper::toUserRoleDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignRole(UUID userId, UserRoleEnum role) throws BadRequestException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if(user.getRoles() != null){
            boolean roleExists = user.getRoles().stream().anyMatch(userRole -> userRole.getRole().equals(role));
            if (roleExists) {
                throw new BadRequestException("User already has role: " + role);
            }
        }

        UserRole newUserRole = UserRole.builder().user(user).role(role).build();
        userRoleRepository.save(newUserRole);
    }

    @Transactional
    public void removeRole(UUID userId, UserRoleEnum role){
        UserRole userRole = userRoleRepository.findByUserIdAndRole(userId, role)
                .orElseThrow(() -> new ResourceNotFoundException("Role '" + role + "' not found for user with id: " + userId));
        userRoleRepository.delete(userRole);
    }
}
