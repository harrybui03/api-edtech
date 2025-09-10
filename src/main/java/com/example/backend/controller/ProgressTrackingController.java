package com.example.backend.controller;

import com.example.backend.dto.request.progress.VideoWatchTimeRequest;
import com.example.backend.dto.response.progress.CourseProgressResponse;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.ProgressTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    private final UserRepository userRepository;
    
    @PostMapping("/lessons/{lessonId}/progress")
    @Operation(summary = "Mark lesson as completed", 
               description = "Mark a lesson as completed and update course progress")
    public ResponseEntity<Void> markLessonCompleted(@PathVariable UUID lessonId) {
        UUID currentUserId = getCurrentUserId();
        progressTrackingService.markLessonCompleted(currentUserId, lessonId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/courses/{courseId}/my-progress")
    @Operation(summary = "Get course progress", 
               description = "Get detailed progress information for a specific course")
    public ResponseEntity<CourseProgressResponse> getCourseProgress(@PathVariable UUID courseId) {
        UUID currentUserId = getCurrentUserId();
        CourseProgressResponse progress = progressTrackingService.getCourseProgress(currentUserId, courseId);
        return ResponseEntity.ok(progress);
    }
    
    @PostMapping("/videos/{lessonId}/watch-time")
    @Operation(summary = "Record video watch time", 
               description = "Record the time spent watching a video lesson")
    public ResponseEntity<Void> recordVideoWatchTime(
            @PathVariable UUID lessonId, 
            @Valid @RequestBody VideoWatchTimeRequest request) {
        UUID currentUserId = getCurrentUserId();
        progressTrackingService.recordVideoWatchTime(currentUserId, lessonId, request);
        return ResponseEntity.ok().build();
    }
    
    private UUID getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        return user.getId();
    }
}
