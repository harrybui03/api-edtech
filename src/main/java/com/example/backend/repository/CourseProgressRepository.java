package com.example.backend.repository;

import com.example.backend.entity.CourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseProgressRepository extends JpaRepository<CourseProgress, UUID> {
    
    Optional<CourseProgress> findByMemberIdAndLessonId(UUID memberId, UUID lessonId);
    
    List<CourseProgress> findByMemberIdAndCourseId(UUID memberId, UUID courseId);
    
    @Query("SELECT COUNT(cp) FROM CourseProgress cp WHERE cp.member.id = :memberId AND cp.course.id = :courseId AND cp.status = 'COMPLETE'")
    long countCompletedLessons(@Param("memberId") UUID memberId, @Param("courseId") UUID courseId);
    
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.course.id = :courseId")
    long countTotalLessons(@Param("courseId") UUID courseId);
}