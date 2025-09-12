package com.example.backend.repository;

import com.example.backend.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    boolean existsBySlug(String slug);
    Optional<Lesson> findBySlug(String slug);
    List<Lesson> findByChapterIdOrderByPosition(UUID chapterId);
}
