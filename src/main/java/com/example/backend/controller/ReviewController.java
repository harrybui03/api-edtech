package com.example.backend.controller;

import com.example.backend.dto.request.review.ReviewRequest;
import com.example.backend.dto.response.review.ReviewResponse;
import com.example.backend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Course review management APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/courses/{courseId}/reviews")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Create course review", description = "Student creates a review for a course they are enrolled in")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable UUID courseId,
            @Valid @RequestBody ReviewRequest request) {
        ReviewResponse review = reviewService.createReview(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Update review", description = "Student updates their own review")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewRequest request) {
        ReviewResponse review = reviewService.updateReview(reviewId, request);
        return ResponseEntity.ok(review);
    }

    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Delete review", description = "Student deletes their own review")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses/{courseId}/my-review")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get my review", description = "Student gets their own review for a course")
    public ResponseEntity<ReviewResponse> getMyReview(@PathVariable UUID courseId) {
        ReviewResponse review = reviewService.getMyReviewForCourse(courseId);
        return ResponseEntity.ok(review);
    }

}
