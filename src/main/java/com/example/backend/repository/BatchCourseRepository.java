package com.example.backend.repository;

import com.example.backend.entity.BatchCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BatchCourseRepository extends JpaRepository<BatchCourse, UUID> {}
