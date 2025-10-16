package com.example.backend.service;

import com.example.backend.constant.CourseStatus;
import com.example.backend.constant.EnrollmentMemberType;
import com.example.backend.constant.EnrollmentRole;
import com.example.backend.dto.response.enrollment.EnrollmentResponse;
import com.example.backend.entity.*;
import com.example.backend.mapper.EnrollmentMapper;
import com.example.backend.repository.CourseRepository;
import com.example.backend.repository.BatchRepository;
import com.example.backend.repository.BatchEnrollmentRepository;
import com.example.backend.repository.EnrollmentRepository;
import com.example.backend.repository.LessonRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
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
public class EnrollmentService {
    
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final BatchRepository batchRepository;
    private final BatchEnrollmentRepository batchEnrollmentRepository;
    

    public EnrollmentResponse enrollInCourseBySlug(String courseSlug) {
        String studentEmail = getCurrentUserEmail();
        log.info("Enrolling student {} in course slug {}", studentEmail, courseSlug);

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Course course = courseRepository.findBySlug(courseSlug)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (enrollmentRepository.existsByMemberIdAndCourseId(student.getId(), course.getId())) {
            throw new RuntimeException("Student is already enrolled in this course");
        }

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new RuntimeException("Course is not available for enrollment");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setMember(student);
        enrollment.setCourse(course);
        enrollment.setMemberType(EnrollmentMemberType.STUDENT);
        enrollment.setRole(EnrollmentRole.MEMBER);
        enrollment.setProgress(BigDecimal.ZERO);

        // Set current lesson to the first lesson of the course
        List<Lesson> lessons = lessonRepository.findByCourseId(course.getId());
        if (!lessons.isEmpty()) {
            // Sort lessons by chapter position and lesson position
            lessons.sort((l1, l2) -> {
                int chapterComparison = l1.getChapter().getPosition().compareTo(l2.getChapter().getPosition());
                if (chapterComparison != 0) return chapterComparison;
                return l1.getPosition().compareTo(l2.getPosition());
            });
            enrollment.setCurrentLesson(lessons.get(0));
        }

        enrollment = enrollmentRepository.save(enrollment);

        course.setEnrollments((course.getEnrollments() != null ? course.getEnrollments() : 0) + 1);
        courseRepository.save(course);

        log.info("Successfully enrolled student {} in course slug {}", studentEmail, courseSlug);

        return enrollmentMapper.toResponse(enrollment);
    }

    public void enrollInBatchBySlug(String batchSlug) {
        String studentEmail = getCurrentUserEmail();
        log.info("Enrolling student {} in batch slug {}", studentEmail, batchSlug);

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Batch batch = batchRepository.findBySlug(batchSlug)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        // Prevent duplicates via repository method
        boolean alreadyEnrolled = batchEnrollmentRepository.existsByUserIdAndBatchId(student.getId(), batch.getId());
        if (alreadyEnrolled) {
            throw new RuntimeException("Student is already enrolled in this batch");
        }

        BatchEnrollment be = new BatchEnrollment();
        be.setUser(student);
        be.setBatch(batch);
        be.setMemberType("STUDENT");
        be.setEnrolledAt(java.time.LocalDateTime.now());

        batchEnrollmentRepository.save(be);
        log.info("Successfully enrolled student {} in batch {}", studentEmail, batchSlug);
    }

    @Transactional(readOnly = true)
    public boolean isPaidBatchBySlug(String batchSlug) {
        Batch batch = batchRepository.findBySlug(batchSlug)
                .orElseThrow(() -> new RuntimeException("Batch not found"));
        return batch.isPaidBatch();
    }
    
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments() {
        String studentEmail = getCurrentUserEmail();
        log.info("Getting enrollments for student {}", studentEmail);
        
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        List<Enrollment> enrollments = enrollmentRepository.findByMemberId(student.getId());
        
        return enrollments.stream()
                .map(enrollmentMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getCourseEnrollments(UUID courseId) {
        String instructorEmail = getCurrentUserEmail();
        log.info("Getting enrollments for course {} by instructor {}", courseId, instructorEmail);
        
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        
        // Verify instructor has access to this course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        boolean isInstructor = course.getInstructors().stream()
                .anyMatch(ci -> ci.getUser().getId().equals(instructor.getId()));
        
        if (!isInstructor) {
            throw new RuntimeException("You are not authorized to view enrollments for this course");
        }
        
        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdAndInstructorId(courseId, instructor.getId());
        
        return enrollments.stream()
                .map(enrollmentMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public void removeEnrollment(UUID enrollmentId) {
        String instructorEmail = getCurrentUserEmail();
        log.info("Removing enrollment {} by instructor {}", enrollmentId, instructorEmail);
        
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        
        // Verify instructor has access to this course
        Course course = enrollment.getCourse();
        boolean isInstructor = course.getInstructors().stream()
                .anyMatch(ci -> ci.getUser().getId().equals(instructor.getId()));
        
        if (!isInstructor) {
            throw new RuntimeException("You are not authorized to remove enrollments from this course");
        }
        
        enrollmentRepository.delete(enrollment);
        
        // Update course enrollment count
        course.setEnrollments((course.getEnrollments() != null ? course.getEnrollments() : 1) - 1);
        courseRepository.save(course);
        
        log.info("Successfully removed enrollment {}", enrollmentId);
    }
    
    @Transactional(readOnly = true)
    public boolean isEnrolled(UUID studentId, UUID courseId) {
        return enrollmentRepository.existsByMemberIdAndCourseId(studentId, courseId);
    }
    
    @Transactional(readOnly = true)
    public boolean isPaidCourseBySlug(String courseSlug) {
        Course course = courseRepository.findBySlug(courseSlug)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return Boolean.TRUE.equals(course.getPaidCourse());
    }
    
    @Transactional
    public void createEnrollment(UUID studentId, UUID courseId) {
        log.info("Creating enrollment for student {} in course {}", studentId, courseId);
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Check if already enrolled
        if (enrollmentRepository.existsByMemberIdAndCourseId(studentId, courseId)) {
            log.warn("Student {} is already enrolled in course {}", studentId, courseId);
            return;
        }
        
        // Create new enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setMember(student);
        enrollment.setCourse(course);
        enrollment.setMemberType(EnrollmentMemberType.STUDENT);
        enrollment.setRole(EnrollmentRole.MEMBER);
        enrollment.setProgress(BigDecimal.ZERO);
        
        enrollmentRepository.save(enrollment);
        
        // Update course enrollment count
        course.setEnrollments((course.getEnrollments() != null ? course.getEnrollments() : 0) + 1);
        courseRepository.save(course);
        
        log.info("Successfully created enrollment for student {} in course {}", studentId, courseId);
    }
    
    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
