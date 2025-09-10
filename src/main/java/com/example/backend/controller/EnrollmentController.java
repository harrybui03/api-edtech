package com.example.backend.controller;

import com.example.backend.dto.response.enrollment.EnrollmentResponse;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Enrollment", description = "Course enrollment management APIs")
public class EnrollmentController {
    
    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;
    
    @PostMapping("/courses/{courseId}/enroll")
    @Operation(summary = "Enroll in a course", description = "Student enrolls in a specific course")
    public ResponseEntity<EnrollmentResponse> enrollInCourse(@PathVariable UUID courseId) {
        UUID currentUserId = getCurrentUserId();
        EnrollmentResponse response = enrollmentService.enrollInCourse(currentUserId, courseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/enrollments/my-courses")
    @Operation(summary = "Get my enrolled courses", description = "Get all courses that the current user has enrolled in")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments() {
        UUID currentUserId = getCurrentUserId();
        List<EnrollmentResponse> enrollments = enrollmentService.getMyEnrollments(currentUserId);
        return ResponseEntity.ok(enrollments);
    }
    
    @GetMapping("/instructor/courses/{courseId}/enrollments")
    @Operation(summary = "Get course enrollments", description = "Instructor gets all enrollments for their course")
    public ResponseEntity<List<EnrollmentResponse>> getCourseEnrollments(@PathVariable UUID courseId) {
        UUID currentUserId = getCurrentUserId();
        List<EnrollmentResponse> enrollments = enrollmentService.getCourseEnrollments(courseId, currentUserId);
        return ResponseEntity.ok(enrollments);
    }
    
    @DeleteMapping("/instructor/enrollments/{enrollmentId}")
    @Operation(summary = "Remove enrollment", description = "Instructor removes a student from their course")
    public ResponseEntity<Void> removeEnrollment(@PathVariable UUID enrollmentId) {
        UUID currentUserId = getCurrentUserId();
        enrollmentService.removeEnrollment(enrollmentId, currentUserId);
        return ResponseEntity.noContent().build();
    }
    
    private UUID getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        return user.getId();
    }
}
