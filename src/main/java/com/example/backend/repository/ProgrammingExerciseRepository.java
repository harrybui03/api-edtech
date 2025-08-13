package com.example.backend.repository;

import com.example.backend.entity.ProgrammingExercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProgrammingExerciseRepository extends JpaRepository<ProgrammingExercise, UUID> {
} 