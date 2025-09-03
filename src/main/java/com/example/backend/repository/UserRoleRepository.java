package com.example.backend.repository;

import com.example.backend.constant.UserRoleEnum;
import com.example.backend.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    List<UserRole> findAllByUserId(UUID userId);

    Optional<UserRole> findByUserIdAndRole(UUID userId, UserRoleEnum role);
}