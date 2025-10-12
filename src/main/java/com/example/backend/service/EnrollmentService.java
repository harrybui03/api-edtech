package com.example.backend.service;

import com.example.backend.constant.CourseStatus;
import com.example.backend.constant.EnrollmentMemberType;
import com.example.backend.constant.EnrollmentRole;
import com.example.backend.dto.response.enrollment.EnrollmentResponse;
import com.example.backend.entity.Course;
import com.example.backend.entity.Enrollment;
import com.example.backend.entity.Lesson;
import com.example.backend.entity.User;
import com.example.backend.mapper.EnrollmentMapper;
import com.example.backend.repository.CourseRepository;
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
    
    public EnrollmentResponse enrollInCourse(UUID courseId) {
        String studentEmail = getCurrentUserEmail();
        log.info("Enrolling student {} in course {}", studentEmail, courseId);
        
        // Validate user and course existence
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        // Check if already enrolled
        if (enrollmentRepository.existsByMemberIdAndCourseId(student.getId(), courseId)) {
            throw new RuntimeException("Student is already enrolled in this course");
        }
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new RuntimeException("Course is not available for enrollment");
        }
        
        // Create new enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setMember(student);
        enrollment.setCourse(course);
        enrollment.setMemberType(EnrollmentMemberType.STUDENT);
        enrollment.setRole(EnrollmentRole.MEMBER);
        enrollment.setProgress(BigDecimal.ZERO);
        
        // Set current lesson to the first lesson of the course
        List<Lesson> lessons = lessonRepository.findByCourseId(courseId);
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
        
        // Update course enrollment count
        course.setEnrollments((course.getEnrollments() != null ? course.getEnrollments() : 0) + 1);
        courseRepository.save(course);
        
        log.info("Successfully enrolled student {} in course {}", studentEmail, courseId);
        
        return enrollmentMapper.toResponse(enrollment);
    }

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
    public boolean isPaidCourse(UUID courseId) {
        Course course = courseRepository.findById(courseId)
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
