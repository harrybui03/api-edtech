package com.example.backend.repository;

import com.example.backend.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    
    List<Quiz> findByCourseId(UUID courseId);
    
    List<Quiz> findByLessonId(UUID lessonId);
    
    @Query("SELECT q FROM Quiz q WHERE q.course.id = :courseId AND " +
           "EXISTS (SELECT 1 FROM CourseInstructor ci WHERE ci.course.id = :courseId AND ci.user.email = :instructorEmail)")
    List<Quiz> findByCourseIdAndInstructorEmail(@Param("courseId") UUID courseId, @Param("instructorEmail") String instructorEmail);
}
