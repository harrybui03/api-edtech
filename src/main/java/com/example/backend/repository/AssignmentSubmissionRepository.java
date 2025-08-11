package com.example.backend.repository;

import com.example.backend.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, UUID> {}