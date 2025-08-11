package com.example.backend.repository;

import com.example.backend.entity.CourseInstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourseInstructorRepository extends JpaRepository<CourseInstructor, UUID> {}
