package com.example.backend.repository;

import com.example.backend.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChapterRepository extends JpaRepository<Chapter, UUID> {
    @Query("SELECT c FROM Chapter c LEFT JOIN FETCH c.lessons WHERE c.course.slug = :slug ORDER BY c.position ASC")
    List<Chapter> findByCourseSlugWithLessonsOrderByPositionAsc(@Param("slug") String slug);
    
    List<Chapter> findByCourseIdOrderByCreation(UUID courseId);

    boolean existsBySlug(String slug);
}
