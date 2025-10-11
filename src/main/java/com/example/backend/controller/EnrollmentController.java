package com.example.backend.controller;

import com.example.backend.dto.response.enrollment.EnrollmentResponse;
import com.example.backend.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Enrollment", description = "Course enrollment management APIs")
public class EnrollmentController {
    
    private final EnrollmentService enrollmentService;
    
    @PostMapping("/courses/{courseId}/enroll")
    @Operation(summary = "Enroll in a course", description = "Student enrolls in a specific course")
    public ResponseEntity<EnrollmentResponse> enrollInCourse(@PathVariable UUID courseId) {
        EnrollmentResponse response = enrollmentService.enrollInCourse(courseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/courses/slug/{courseSlug}/enroll")
    @Operation(summary = "Enroll in a course by slug", description = "Student enrolls in a specific course using course slug (SEO)")
    public ResponseEntity<EnrollmentResponse> enrollInCourseBySlug(@PathVariable String courseSlug) {
        EnrollmentResponse response = enrollmentService.enrollInCourseBySlug(courseSlug);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/enrollments/my-courses")
    @Operation(summary = "Get my enrolled courses", description = "Get all courses that the current user has enrolled in")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments() {
        List<EnrollmentResponse> enrollments = enrollmentService.getMyEnrollments();
        return ResponseEntity.ok(enrollments);
    }
}
