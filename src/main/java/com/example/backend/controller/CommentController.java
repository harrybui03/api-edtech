package com.example.backend.controller;

import com.example.backend.dto.request.comment.CommentRequest;
import com.example.backend.dto.response.comment.CommentResponse;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Lesson comment management APIs")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/lessons/{lessonId}/comments")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR')")
    @Operation(summary = "Create comment", description = "Create a root comment on a lesson")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable UUID lessonId,
            @Valid @RequestBody CommentRequest request) {
        CommentResponse comment = commentService.createComment(lessonId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @PostMapping("/comments/{commentId}/replies")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR')")
    @Operation(summary = "Create reply", description = "Create a reply to an existing comment")
    public ResponseEntity<CommentResponse> createReply(
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentRequest request) {
        CommentResponse reply = commentService.createReply(commentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reply);
    }

    @GetMapping("/lessons/{lessonId}/comments")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR')")
    @Operation(summary = "Get lesson comments", description = "Get all comments for a lesson with threading")
    public ResponseEntity<Page<CommentResponse>> getCommentsForLesson(
            @PathVariable UUID lessonId,
            Pageable pageable) {
        Page<CommentResponse> comments = commentService.getCommentsForLesson(lessonId, pageable);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/comments/{commentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR')")
    @Operation(summary = "Update comment", description = "Update own comment content")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentRequest request) {
        CommentResponse comment = commentService.updateComment(commentId, request);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Delete comment", description = "Delete a comment (soft delete)")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comments/{commentId}/upvote")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR')")
    @Operation(summary = "Upvote comment", description = "Add or toggle upvote on a comment")
    public ResponseEntity<CommentResponse> upvoteComment(@PathVariable UUID commentId) {
        CommentResponse comment = commentService.upvoteComment(commentId);
        return ResponseEntity.ok(comment);
    }

    @PostMapping("/comments/{commentId}/downvote")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR')")
    @Operation(summary = "Downvote comment", description = "Add or toggle downvote on a comment")
    public ResponseEntity<CommentResponse> downvoteComment(@PathVariable UUID commentId) {
        CommentResponse comment = commentService.downvoteComment(commentId);
        return ResponseEntity.ok(comment);
    }
}
