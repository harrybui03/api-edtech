package com.example.backend.repository;

import com.example.backend.entity.CourseInstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CourseInstructorRepository extends JpaRepository<CourseInstructor, UUID> {
    
    @Query("SELECT COUNT(ci) > 0 FROM CourseInstructor ci WHERE ci.course.id = :courseId AND ci.user.email = :email")
    boolean existsByCourseIdAndInstructorEmail(@Param("courseId") UUID courseId, @Param("email") String email);
}
