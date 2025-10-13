package com.example.backend.controller;

import com.example.backend.dto.response.enrollment.EnrollmentResponse;
import com.example.backend.dto.response.payment.PaymentResponse;
import com.example.backend.service.EnrollmentService;
import com.example.backend.service.PaymentService;
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
    private final PaymentService paymentService;
    
    @PostMapping("/courses/{courseId}/enroll-free")
    @Operation(summary = "Enroll in a free course", description = "Student enrolls in a free course without payment")
    public ResponseEntity<EnrollmentResponse> enrollInFreeCourse(@PathVariable UUID courseId) {
        if (enrollmentService.isPaidCourse(courseId)) {
            throw new RuntimeException("This course requires payment. Please use the paid enrollment endpoint.");
        }
        
        EnrollmentResponse response = enrollmentService.enrollInCourse(courseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/courses/{courseId}/enroll-paid")
    @Operation(summary = "Enroll in a paid course", description = "Student enrolls in a paid course and creates payment request")
    public ResponseEntity<PaymentResponse> enrollInPaidCourse(@PathVariable UUID courseId) {
        
        if (!enrollmentService.isPaidCourse(courseId)) {
            throw new RuntimeException("This course is free. Please use the free enrollment endpoint.");
        }
        
        PaymentResponse response = paymentService.createPayment(courseId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
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
