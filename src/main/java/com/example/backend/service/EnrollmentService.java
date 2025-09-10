package com.example.backend.service;

import com.example.backend.dto.response.enrollment.EnrollmentResponse;

import java.util.List;
import java.util.UUID;

public interface EnrollmentService {
    
    /**
     * Enroll a student in a course
     */
    EnrollmentResponse enrollInCourse(UUID studentId, UUID courseId);
    
    /**
     * Get all courses enrolled by a student
     */
    List<EnrollmentResponse> getMyEnrollments(UUID studentId);
    
    /**
     * Get all enrollments for a specific course (instructor only)
     */
    List<EnrollmentResponse> getCourseEnrollments(UUID courseId, UUID instructorId);
    
    /**
     * Remove a student from a course (instructor only)
     */
    void removeEnrollment(UUID enrollmentId, UUID instructorId);
    
    /**
     * Check if a student is enrolled in a course
     */
    boolean isEnrolled(UUID studentId, UUID courseId);
}
