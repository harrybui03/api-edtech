package com.example.backend.repository;

import com.example.backend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // Find all root comments for a lesson (comments without parent) that are not deleted
    @Query("SELECT c FROM Comment c WHERE c.lesson.id = :lessonId AND c.parent IS NULL AND c.isDeleted = false ORDER BY c.creation DESC")
    Page<Comment> findRootCommentsByLessonId(@Param("lessonId") UUID lessonId, Pageable pageable);

    // Find all comments for a lesson including replies
    @Query("SELECT c FROM Comment c WHERE c.lesson.id = :lessonId AND c.isDeleted = false ORDER BY c.creation DESC")
    List<Comment> findAllCommentsByLessonId(@Param("lessonId") UUID lessonId);

    // Find replies for a specific comment
    @Query("SELECT c FROM Comment c WHERE c.parent.id = :parentId AND c.isDeleted = false ORDER BY c.creation ASC")
    List<Comment> findRepliesByParentId(@Param("parentId") UUID parentId);

    // Find comments by author
    Page<Comment> findByAuthorIdAndIsDeletedFalseOrderByCreationDesc(UUID authorId, Pageable pageable);

    // Count comments for a lesson
    long countByLessonIdAndIsDeletedFalse(UUID lessonId);

    // Count replies for a comment
    long countByParentIdAndIsDeletedFalse(UUID parentId);

    // Find all comments for admin purposes (including deleted ones)
    Page<Comment> findAllByOrderByCreationDesc(Pageable pageable);
}
