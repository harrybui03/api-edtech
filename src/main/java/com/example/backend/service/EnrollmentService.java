package com.example.backend.service;

import com.example.backend.constant.CourseStatus;
import com.example.backend.constant.EnrollmentMemberType;
import com.example.backend.constant.EnrollmentRole;
import com.example.backend.dto.request.enrollment.CurrentEnrollmentRequest;
import com.example.backend.dto.response.enrollment.CurrentEnrollmentResponse;
import com.example.backend.dto.response.enrollment.EnrollmentResponse;
import com.example.backend.entity.*;
import com.example.backend.excecption.DataNotFoundException;
import com.example.backend.excecption.InvalidRequestDataException;
import com.example.backend.excecption.ResourceNotFoundException;
import com.example.backend.mapper.EnrollmentMapper;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
private final LiveSessionRepository liveSessionRepository;

    // ... (other methods remain the same)
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

        List<Lesson> lessons = lessonRepository.findByCourseId(course.getId());
        if (!lessons.isEmpty()) {
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

        boolean alreadyEnrolled = batchEnrollmentRepository.existsByUserIdAndBatchId(student.getId(), batch.getId());
        if (alreadyEnrolled) {
            throw new RuntimeException("Student is already enrolled in this batch");
        }

        BatchEnrollment be = new BatchEnrollment();
        be.setUser(student);
        be.setBatch(batch);
        be.setMemberType("STUDENT");
        be.setEnrolledAt(OffsetDateTime.now());

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
    public Page<CurrentEnrollmentResponse> getCurrentUserEnrollments(CurrentEnrollmentRequest currentEnrollmentRequest) {
        String studentEmail = getCurrentUserEmail();
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new DataNotFoundException("Student not found"));

        String sortBy = currentEnrollmentRequest.getFilterBy().equals(CurrentEnrollmentRequest.FilterBy.COURSE) ? "creation" : "enrolledAt";
        Pageable pageable = PageRequest.of(currentEnrollmentRequest.getPage(), currentEnrollmentRequest.getSize(), Sort.by(sortBy).descending());

        if (currentEnrollmentRequest.getFilterBy().equals(CurrentEnrollmentRequest.FilterBy.BATCH)) {

            return batchEnrollmentRepository.findByMemberId(student.getId(), pageable).map(enrollment -> CurrentEnrollmentResponse
                    .builder()
                    .enrollmentDate(enrollment.getEnrolledAt())
                    .courseTitle(enrollment.getBatch().getTitle())
                    .price(enrollment.getBatch().getAmountUsd()).build());
        }

        return enrollmentRepository.findByMemberId(student.getId() , pageable).map(enrollment ->
                CurrentEnrollmentResponse
                        .builder()
                        .enrollmentDate(enrollment.getCreation())
                        .courseTitle(enrollment.getCourse().getTitle())
                        .price(enrollment.getCourse().getCoursePrice()).build()
        );
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getCourseEnrollments(UUID courseId) {
        String instructorEmail = getCurrentUserEmail();
        log.info("Getting enrollments for course {} by instructor {}", courseId, instructorEmail);

        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));


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


        Course course = enrollment.getCourse();
        boolean isInstructor = course.getInstructors().stream()
                .anyMatch(ci -> ci.getUser().getId().equals(instructor.getId()));

        if (!isInstructor) {
            throw new RuntimeException("You are not authorized to remove enrollments from this course");
        }

        enrollmentRepository.delete(enrollment);


        course.setEnrollments((course.getEnrollments() != null ? course.getEnrollments() : 1) - 1);
        courseRepository.save(course);

        log.info("Successfully removed enrollment {}", enrollmentId);
    }

    @Transactional(readOnly = true)
    public boolean isEnrolled(UUID studentId, UUID courseId) {
        return enrollmentRepository.existsByMemberIdAndCourseId(studentId, courseId);
    }
@Transactional(readOnly = true)
    public boolean isUserAuthorizedForSession(String email, UUID sessionId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        LiveSession liveSession = liveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("LiveSession not found with ID: " + sessionId));

        Batch batch = liveSession.getBatch();
        if (batch == null) {
            throw new InvalidRequestDataException("LiveSession with ID: " + sessionId + " is not associated with any batch.");
        }
        UUID batchId = batch.getId();

        // Check 1: Is the user an enrolled student in the batch?
        boolean isEnrolledStudent = batchEnrollmentRepository.existsByUserIdAndBatchId(user.getId(), batchId);
        if (isEnrolledStudent) {
            return true;
        }

        // Check 2: Is the user an instructor for the batch?
        boolean isInstructor = batch.getInstructors().stream()
                .anyMatch(batchInstructor -> batchInstructor.getInstructor().getId().equals(user.getId()));

        if (isInstructor) {
            return true;
        }

        return false;
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


        if (enrollmentRepository.existsByMemberIdAndCourseId(studentId, courseId)) {
            log.warn("Student {} is already enrolled in course {}", studentId, courseId);
            return;
        }


        Enrollment enrollment = new Enrollment();
        enrollment.setMember(student);
        enrollment.setCourse(course);
        enrollment.setMemberType(EnrollmentMemberType.STUDENT);
        enrollment.setRole(EnrollmentRole.MEMBER);
        enrollment.setProgress(BigDecimal.ZERO);

        enrollmentRepository.save(enrollment);


        course.setEnrollments((course.getEnrollments() != null ? course.getEnrollments() : 0) + 1);
        courseRepository.save(course);

        log.info("Successfully created enrollment for student {} in course {}", studentId, courseId);
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
