package com.example.backend.controller;

import com.example.backend.dto.model.*;
import com.example.backend.dto.response.pagination.PaginationResponse;
import com.example.backend.dto.response.review.ReviewResponse;
import com.example.backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {
    private final CourseService courseService;
    private final ChapterService chapterService;
    private final ReviewService reviewService;
    private final LessonService lessonService;
    private final UserService userService;
    private final BatchService batchService;

    @GetMapping("/courses")
    public ResponseEntity<PaginationResponse<CoursePublicDto>> getPublishedCourses(
            Pageable pageable,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) List<String> labels,
            @RequestParam(required = false) String search) {
        Page<CoursePublicDto> courses = courseService.getPublishedCourses(pageable, tags, labels, search);
        return ResponseEntity.ok(new PaginationResponse<>(courses));
    }

    @GetMapping("/courses/{slug}")
    public ResponseEntity<CoursePublicDto> getCourseDetails(@PathVariable String slug) {
        return ResponseEntity.ok(courseService.getCourseBySlugPublic(slug));
    }

    @GetMapping("/courses/{slug}/chapters")
    public ResponseEntity<List<ChapterPublicDto>> getCourseChapters(@PathVariable String slug) {
        return ResponseEntity.ok(chapterService.getChaptersByCoursePublic(slug));
    }

    @GetMapping("/courses/{courseSlug}/reviews")
    public ResponseEntity<PaginationResponse<ReviewResponse>> getCourseReviews(
            @PathVariable String courseSlug,
            Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getApprovedReviewsByCourseSlug(courseSlug, pageable);
        return ResponseEntity.ok(new PaginationResponse<>(reviews));
    }

    @GetMapping("/courses/{courseSlug}/average-rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable String courseSlug) {
        Double avg = reviewService.getAverageRatingForCourseSlug(courseSlug);
        return ResponseEntity.ok(avg != null ? avg : 0.0);
    }

    @GetMapping("/lessons/{lessonSlug}")
    public ResponseEntity<LessonPublicDto> getLessonPublic(@PathVariable String lessonSlug) {
        return ResponseEntity.ok(lessonService.getLessonBySlugPublic(lessonSlug));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/batches")
    public ResponseEntity<PaginationResponse<BatchDto>> getPublishedBatches(
            Pageable pageable,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) List<String> labels,
            @RequestParam(required = false) String search) {
        Page<BatchDto> batches = batchService.getPublishedBatches(pageable, tags, labels, search);
        return ResponseEntity.ok(new PaginationResponse<>(batches));
    }

    @GetMapping("/batches/{slug}")
    public ResponseEntity<BatchDto> getBatchDetails(@PathVariable String slug) {
        return ResponseEntity.ok(batchService.getBatchBySlug(slug));
    }
}
