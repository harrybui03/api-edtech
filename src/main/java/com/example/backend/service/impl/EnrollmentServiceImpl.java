package com.example.backend.service.impl;

import com.example.backend.constant.EnrollmentMemberType;
import com.example.backend.constant.EnrollmentRole;
import com.example.backend.dto.response.enrollment.EnrollmentResponse;
import com.example.backend.entity.Course;
import com.example.backend.entity.Enrollment;
import com.example.backend.entity.User;
import com.example.backend.repository.CourseRepository;
import com.example.backend.repository.EnrollmentRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {
    
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    
    @Override
    public EnrollmentResponse enrollInCourse(UUID studentId, UUID courseId) {
        log.info("Enrolling student {} in course {}", studentId, courseId);
        
        // Check if already enrolled
        if (enrollmentRepository.existsByMemberIdAndCourseId(studentId, courseId)) {
            throw new RuntimeException("Student is already enrolled in this course");
        }
        
        // Validate user and course existence
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Check if course is published and available for enrollment
        if (!Boolean.TRUE.equals(course.getPublished())) {
            throw new RuntimeException("Course is not available for enrollment");
        }
        
        // Create new enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setMember(student);
        enrollment.setCourse(course);
        enrollment.setMemberType(EnrollmentMemberType.STUDENT);
        enrollment.setRole(EnrollmentRole.MEMBER);
        enrollment.setProgress(BigDecimal.ZERO);
        enrollment.setPurchasedCertificate(false);
        
        enrollment = enrollmentRepository.save(enrollment);
        
        // Update course enrollment count
        course.setEnrollments((course.getEnrollments() != null ? course.getEnrollments() : 0) + 1);
        courseRepository.save(course);
        
        log.info("Successfully enrolled student {} in course {}", studentId, courseId);
        
        return mapToEnrollmentResponse(enrollment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments(UUID studentId) {
        log.info("Getting enrollments for student {}", studentId);
        
        List<Enrollment> enrollments = enrollmentRepository.findByMemberId(studentId);
        
        return enrollments.stream()
                .map(this::mapToEnrollmentResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getCourseEnrollments(UUID courseId, UUID instructorId) {
        log.info("Getting enrollments for course {} by instructor {}", courseId, instructorId);
        
        // Verify instructor has access to this course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        boolean isInstructor = course.getInstructors().stream()
                .anyMatch(ci -> ci.getUser().getId().equals(instructorId));
        
        if (!isInstructor) {
            throw new RuntimeException("You are not authorized to view enrollments for this course");
        }
        
        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdAndInstructorId(courseId, instructorId);
        
        return enrollments.stream()
                .map(this::mapToEnrollmentResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public void removeEnrollment(UUID enrollmentId, UUID instructorId) {
        log.info("Removing enrollment {} by instructor {}", enrollmentId, instructorId);
        
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        
        // Verify instructor has access to this course
        Course course = enrollment.getCourse();
        boolean isInstructor = course.getInstructors().stream()
                .anyMatch(ci -> ci.getUser().getId().equals(instructorId));
        
        if (!isInstructor) {
            throw new RuntimeException("You are not authorized to remove enrollments from this course");
        }
        
        enrollmentRepository.delete(enrollment);
        
        // Update course enrollment count
        course.setEnrollments((course.getEnrollments() != null ? course.getEnrollments() : 1) - 1);
        courseRepository.save(course);
        
        log.info("Successfully removed enrollment {}", enrollmentId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isEnrolled(UUID studentId, UUID courseId) {
        return enrollmentRepository.existsByMemberIdAndCourseId(studentId, courseId);
    }
    
    private EnrollmentResponse mapToEnrollmentResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .memberId(enrollment.getMember().getId())
                .memberName(enrollment.getMember().getFullName())
                .memberEmail(enrollment.getMember().getEmail())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .memberType(enrollment.getMemberType())
                .role(enrollment.getRole())
                .progress(enrollment.getProgress())
                .currentLessonId(enrollment.getCurrentLesson() != null ? enrollment.getCurrentLesson().getId() : null)
                .currentLessonTitle(enrollment.getCurrentLesson() != null ? enrollment.getCurrentLesson().getTitle() : null)
                .enrolledAt(enrollment.getCreation())
                .build();
    }
}
