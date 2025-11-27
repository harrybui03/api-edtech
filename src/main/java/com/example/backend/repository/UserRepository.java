package com.example.backend.repository;

import com.example.backend.constant.UserRoleEnum;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository  extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.role = :role")
    List<User> findByRole(@Param("role") UserRoleEnum role);
}
