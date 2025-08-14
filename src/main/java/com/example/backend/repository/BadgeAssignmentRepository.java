package com.example.backend.repository;

import com.example.backend.entity.BadgeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BadgeAssignmentRepository extends JpaRepository<BadgeAssignment, UUID> {
} 