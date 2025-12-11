package com.example.backend.controller;

import com.example.backend.dto.request.enrollment.CurrentEnrollmentRequest;
import com.example.backend.dto.response.enrollment.CurrentEnrollmentResponse;
import com.example.backend.dto.response.enrollment.EnrollmentResponse;
import com.example.backend.dto.response.live.EnrolledBatchResponse;
import com.example.backend.dto.response.payment.PaymentResponse;
import com.example.backend.service.EnrollmentService;
import com.example.backend.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Enrollment", description = "Course enrollment management APIs")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final PaymentService paymentService;

    @PostMapping("/courses/{courseSlug}/enroll-free")
    @Operation(summary = "Enroll in a free course", description = "Student enrolls in a free course without payment (by slug)")
    public ResponseEntity<EnrollmentResponse> enrollInFreeCourse(@PathVariable String courseSlug) {
        if (enrollmentService.isPaidCourseBySlug(courseSlug)) {
            throw new RuntimeException("This course requires payment. Please use the paid enrollment endpoint.");
        }

        EnrollmentResponse response = enrollmentService.enrollInCourseBySlug(courseSlug);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/courses/{courseSlug}/enroll-paid")
    @Operation(summary = "Enroll in a paid course", description = "Student enrolls in a paid course and creates payment request (by slug)")
    public ResponseEntity<PaymentResponse> enrollInPaidCourse(@PathVariable String courseSlug) {
        if (!enrollmentService.isPaidCourseBySlug(courseSlug)) {
            throw new RuntimeException("This course is free. Please use the free enrollment endpoint.");
        }

        PaymentResponse response = paymentService.createPaymentBySlug(courseSlug);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PostMapping("/batches/{batchSlug}/enroll-free")
    @Operation(summary = "Enroll in a free batch", description = "Student enrolls in a free batch/cohort using slug")
    public ResponseEntity<Void> enrollInFreeBatchBySlug(@PathVariable String batchSlug) {
        if (enrollmentService.isPaidBatchBySlug(batchSlug)) {
            throw new RuntimeException("This batch requires payment. Please use the paid enrollment endpoint.");
        }
        enrollmentService.enrollInBatchBySlug(batchSlug);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/batches/{batchSlug}/enroll-paid")
    @Operation(summary = "Enroll in a paid batch", description = "Student enrolls in a paid batch/cohort using slug and creates payment request")
    public ResponseEntity<PaymentResponse> enrollInPaidBatchBySlug(@PathVariable String batchSlug) {
        if (!enrollmentService.isPaidBatchBySlug(batchSlug)) {
            throw new RuntimeException("This batch is free. Please use the free enrollment endpoint.");
        }
        PaymentResponse response = paymentService.createBatchPaymentBySlug(batchSlug);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/enrollments/me")
    @Operation(summary = "Get current user enrollments", description = "Get the enrollment of course or batch current user enroll")
    public ResponseEntity<Page<CurrentEnrollmentResponse>> getCurrentUserEnrollments(
            @RequestParam(required = false) CurrentEnrollmentRequest.FilterBy filterBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CurrentEnrollmentResponse> enrollments = enrollmentService.getCurrentUserEnrollments(CurrentEnrollmentRequest.builder()
                .page(page)
                .filterBy(filterBy == null ? CurrentEnrollmentRequest.FilterBy.COURSE : filterBy)
                .size(size)
                .build());
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/enrollments/my-batches")
    @Operation(summary = "Get my enrolled batches", description = "Get all batches that the current user has enrolled in")
    public ResponseEntity<Page<CurrentEnrollmentResponse>> getMyBatchEnrollments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CurrentEnrollmentResponse> enrollments = enrollmentService.getCurrentUserEnrollments(CurrentEnrollmentRequest.builder()
                .page(page)
                .size(size)
                .filterBy(CurrentEnrollmentRequest.FilterBy.BATCH)
                .build());
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/enrollments/enrolled-batches")
    @Operation(summary = "List enrolled batches (basic info)", description = "Return array of batches (id, slug, title, startTime, endTime) that current user enrolled")
    public ResponseEntity<List<EnrolledBatchResponse>> getEnrolledBatchesSimple() {
        List<EnrolledBatchResponse> batches = enrollmentService.getMyEnrolledBatches();
        return ResponseEntity.ok(batches);
    }

    @GetMapping("/enrollments/my-courses")
    @Operation(summary = "Get my enrolled courses", description = "Get all courses that the current user has enrolled in")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments() {
        List<EnrollmentResponse> enrollments = enrollmentService.getMyEnrollments();
        return ResponseEntity.ok(enrollments);
    }
}
