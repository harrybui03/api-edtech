package com.example.backend.repository;

import com.example.backend.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    boolean existsByMemberIdAndCourseId(UUID memberId, UUID courseId);
}
