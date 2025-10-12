package com.example.backend.controller;

import com.example.backend.dto.request.comment.CommentRequest;
import com.example.backend.dto.request.comment.VoteRequest;
import com.example.backend.dto.response.comment.CommentResponse;
import com.example.backend.dto.response.comment.VoteResponse;
import com.example.backend.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Lesson comment management APIs")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/lessons/{lessonId}/comments")
    @PreAuthorize("hasAnyRole('LMS_STUDENT', 'COURSE_CREATOR')")
    @Operation(summary = "Create comment", description = "Create a comment on a lesson. Include parentId in request body to create a reply to existing comment.")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable UUID lessonId,
            @Valid @RequestBody CommentRequest request) {
        CommentResponse comment = commentService.createComment(lessonId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @GetMapping("/lessons/{lessonId}/comments")
    @PreAuthorize("hasAnyRole('LMS_STUDENT', 'COURSE_CREATOR')")
    @Operation(summary = "Get lesson comments", description = "Get paginated comments for a lesson including replies (use parentId to identify replies)")
    public ResponseEntity<Page<CommentResponse>> getCommentsForLesson(
            @PathVariable UUID lessonId,
            Pageable pageable) {
        Page<CommentResponse> comments = commentService.getCommentsForLesson(lessonId, pageable);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/lessons/slug/{lessonSlug}/comments")
    @PreAuthorize("hasAnyRole('LMS_STUDENT', 'COURSE_CREATOR')")
    @Operation(summary = "Create comment by lesson slug", description = "Create a comment using lesson slug (SEO)")
    public ResponseEntity<CommentResponse> createCommentByLessonSlug(
            @PathVariable String lessonSlug,
            @Valid @RequestBody CommentRequest request) {
        CommentResponse comment = commentService.createCommentByLessonSlug(lessonSlug, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @GetMapping("/lessons/slug/{lessonSlug}/comments")
    @PreAuthorize("hasAnyRole('LMS_STUDENT', 'COURSE_CREATOR')")
    @Operation(summary = "Get lesson comments by slug", description = "Get comments for a lesson using lesson slug")
    public ResponseEntity<Page<CommentResponse>> getCommentsForLessonBySlug(
            @PathVariable String lessonSlug,
            Pageable pageable) {
        Page<CommentResponse> comments = commentService.getCommentsForLessonBySlug(lessonSlug, pageable);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/comments/{commentId}")
    @PreAuthorize("hasAnyRole('LMS_STUDENT', 'COURSE_CREATOR')")
    @Operation(summary = "Update comment", description = "Update own comment content")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentRequest request) {
        CommentResponse comment = commentService.updateComment(commentId, request);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("hasAnyRole('LMS_STUDENT', 'COURSE_CREATOR', 'SYSTEM_MANAGER', 'MODERATOR')")
    @Operation(summary = "Delete comment", description = "Delete a comment (soft delete)")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comments/{commentId}/vote")
    @PreAuthorize("hasAnyRole('LMS_STUDENT', 'COURSE_CREATOR')")
    @Operation(summary = "Vote on comment", description = "Add, change, or toggle vote on a comment (true = UPVOTE, false = DOWNVOTE)")
    public ResponseEntity<CommentResponse> voteComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody VoteRequest request) {
        CommentResponse comment = commentService.voteComment(commentId, request.getIsUpvote());
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/comments/{commentId}")
    @PreAuthorize("hasAnyRole('LMS_STUDENT', 'COURSE_CREATOR')")
    @Operation(summary = "Get comment by ID", description = "Get a specific comment by its ID (useful for replies)")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable UUID commentId) {
        CommentResponse comment = commentService.getCommentById(commentId);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/users/{userId}/votes")
    @PreAuthorize("hasAnyRole('LMS_STUDENT', 'COURSE_CREATOR', 'SYSTEM_MANAGER', 'MODERATOR')")
    @Operation(summary = "Get votes by user ID", description = "Get all votes made by a specific user (users can only view their own votes)")
    public ResponseEntity<List<VoteResponse>> getVotesByUserId(@PathVariable UUID userId) {
        List<VoteResponse> votes = commentService.getVotesByUserId(userId);
        return ResponseEntity.ok(votes);
    }

    @GetMapping("/users/{userId}/lessons/{lessonId}/votes")
    @PreAuthorize("hasAnyRole('LMS_STUDENT', 'COURSE_CREATOR', 'SYSTEM_MANAGER', 'MODERATOR')")
    @Operation(summary = "Get votes by user ID and lesson ID", description = "Get all votes made by a specific user for comments in a specific lesson")
    public ResponseEntity<List<VoteResponse>> getVotesByUserIdAndLessonId(
            @PathVariable UUID userId,
            @PathVariable UUID lessonId) {
        List<VoteResponse> votes = commentService.getVotesByUserIdAndLessonId(userId, lessonId);
        return ResponseEntity.ok(votes);
    }

}
