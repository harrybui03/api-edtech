package com.example.backend.repository;

import com.example.backend.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    boolean existsBySlug(String slug);
    Optional<Lesson> findBySlug(String slug);
    List<Lesson> findByChapterIdOrderByPosition(UUID chapterId);
    List<Lesson> findByCourseId(UUID courseId);
    
    @Query("SELECT l FROM Lesson l WHERE l.course.id = :courseId AND l.id NOT IN " +
           "(SELECT cp.lesson.id FROM CourseProgress cp WHERE cp.member.id = :memberId AND cp.course.id = :courseId AND cp.status = 'COMPLETE') " +
           "ORDER BY l.chapter.position, l.position")
    List<Lesson> findIncompleteLessonsByCourseIdAndMemberId(@Param("courseId") UUID courseId, @Param("memberId") UUID memberId);
}
