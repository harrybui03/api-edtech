package com.example.backend.repository;

import com.example.backend.entity.ProgrammingExerciseSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProgrammingExerciseSubmissionRepository extends JpaRepository<ProgrammingExerciseSubmission, UUID> {
} 