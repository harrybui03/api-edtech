package com.example.backend.controller;

import com.example.backend.dto.response.progress.CourseProgressResponse;
import com.example.backend.service.ProgressTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Progress Tracking", description = "Student progress tracking APIs")
public class ProgressTrackingController {
    
    private final ProgressTrackingService progressTrackingService;
    
    @PostMapping("/lessons/{lessonId}/progress")
    @Operation(summary = "Mark lesson as completed", 
               description = "Mark a lesson as completed and update course progress")
    public ResponseEntity<Void> markLessonCompleted(@PathVariable UUID lessonId) {
        String currentUserEmail = getCurrentUserEmail();
        progressTrackingService.markLessonCompleted(currentUserEmail, lessonId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/courses/{courseId}/my-progress")
    @Operation(summary = "Get course progress", 
               description = "Get detailed progress information for a specific course")
    public ResponseEntity<CourseProgressResponse> getCourseProgress(@PathVariable UUID courseId) {
        String currentUserEmail = getCurrentUserEmail();
        CourseProgressResponse progress = progressTrackingService.getCourseProgress(currentUserEmail, courseId);
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/lessons/slug/{lessonSlug}/progress")
    @Operation(summary = "Mark lesson as completed by slug", 
               description = "Mark a lesson as completed using lesson slug and update course progress")
    public ResponseEntity<Void> markLessonCompletedBySlug(@PathVariable String lessonSlug) {
        String currentUserEmail = getCurrentUserEmail();
        progressTrackingService.markLessonCompletedBySlug(currentUserEmail, lessonSlug);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/courses/slug/{courseSlug}/my-progress")
    @Operation(summary = "Get course progress by slug", 
               description = "Get detailed progress information for a specific course using course slug")
    public ResponseEntity<CourseProgressResponse> getCourseProgressBySlug(@PathVariable String courseSlug) {
        String currentUserEmail = getCurrentUserEmail();
        CourseProgressResponse progress = progressTrackingService.getCourseProgressBySlug(currentUserEmail, courseSlug);
        return ResponseEntity.ok(progress);
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
