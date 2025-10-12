package com.example.backend.repository;

import com.example.backend.entity.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentVoteRepository extends JpaRepository<CommentVote, UUID> {

    // Find user's vote for a specific comment
    Optional<CommentVote> findByCommentIdAndUserId(UUID commentId, UUID userId);

    // Find all votes for comments in a lesson (for efficient loading)
    @Query("SELECT cv FROM CommentVote cv WHERE cv.comment.lesson.id = :lessonId")
    List<CommentVote> findAllVotesByLessonId(@Param("lessonId") UUID lessonId);

    // Find all votes for a specific comment
    List<CommentVote> findByCommentId(UUID commentId);

    // Count upvotes for a comment (voteType = true)
    long countByCommentIdAndVoteType(UUID commentId, Boolean voteType);

    // Count upvotes for a comment
    @Query("SELECT COUNT(cv) FROM CommentVote cv WHERE cv.comment.id = :commentId AND cv.voteType = true")
    long countUpvotesByCommentId(@Param("commentId") UUID commentId);

    // Count downvotes for a comment
    @Query("SELECT COUNT(cv) FROM CommentVote cv WHERE cv.comment.id = :commentId AND cv.voteType = false")
    long countDownvotesByCommentId(@Param("commentId") UUID commentId);

    // Delete user's vote for a comment (for vote changes)
    void deleteByCommentIdAndUserId(UUID commentId, UUID userId);

    // Check if user has already voted for a comment
    boolean existsByCommentIdAndUserId(UUID commentId, UUID userId);

    // Find all votes by a specific user
    List<CommentVote> findByUserId(UUID userId);

    // Find all votes by a specific user for comments in a lesson
    @Query("SELECT cv FROM CommentVote cv WHERE cv.user.id = :userId AND cv.comment.lesson.id = :lessonId")
    List<CommentVote> findByUserIdAndLessonId(@Param("userId") UUID userId, @Param("lessonId") UUID lessonId);
}
