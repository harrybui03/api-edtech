package com.example.backend.repository;

import com.example.backend.entity.CourseInstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CourseInstructorRepository extends JpaRepository<CourseInstructor, UUID> {
    @Modifying
    @Query("DELETE FROM CourseInstructor ci WHERE ci.course.id = :courseId AND ci.user.id IN :instructorIds")
    void deleteByCourseIdAndUserIdIn(@Param("courseId") UUID courseId, @Param("instructorIds") Iterable<UUID> instructorIds);
}
