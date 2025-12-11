package com.example.backend.service;

import com.example.backend.constant.CourseStatus;
import com.example.backend.constant.EnrollmentMemberType;
import com.example.backend.constant.EnrollmentRole;
import com.example.backend.constant.TransactionStatus;
import com.example.backend.dto.request.enrollment.CurrentEnrollmentRequest;
import com.example.backend.dto.response.enrollment.CurrentEnrollmentResponse;
import com.example.backend.dto.response.enrollment.EnrollmentResponse;
import com.example.backend.dto.response.enrollment.BatchEnrollmentResponse;
import com.example.backend.dto.response.live.EnrolledBatchResponse;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Comparator;
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
    private final TransactionRepository transactionRepository;

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
    public List<EnrolledBatchResponse> getMyEnrolledBatches() {
        String studentEmail = getCurrentUserEmail();
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<Batch> batches = batchEnrollmentRepository.findBatchesByUserId(student.getId());

        return batches.stream()
                .map(batch -> EnrolledBatchResponse.builder()
                        .id(batch.getId())
                        .slug(batch.getSlug())
                        .title(batch.getTitle())
                        .startTime(batch.getStartTime())
                        .endTime(batch.getEndTime())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<CurrentEnrollmentResponse> getCurrentUserEnrollments(CurrentEnrollmentRequest currentEnrollmentRequest) {
        String studentEmail = getCurrentUserEmail();
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new DataNotFoundException("Student not found"));

        int page = currentEnrollmentRequest.getPage();
        int size = currentEnrollmentRequest.getSize();

        List<CurrentEnrollmentResponse> responses = new ArrayList<>();

        if (currentEnrollmentRequest.getFilterBy().equals(CurrentEnrollmentRequest.FilterBy.BATCH)) {
            // Paid batch enrollments from transactions
            List<Transaction> batchTransactions = transactionRepository
                    .findByStudentIdAndStatus(student.getId(), TransactionStatus.PAID, Pageable.unpaged())
                    .getContent()
                    .stream()
                    .filter(t -> t.getBatch() != null)
                    .collect(Collectors.toList());

            Set<UUID> paidBatchIds = batchTransactions.stream()
                    .map(t -> t.getBatch().getId())
                    .collect(Collectors.toCollection(HashSet::new));

            for (Transaction tx : batchTransactions) {
                OffsetDateTime enrollmentDate = tx.getPaidAt() != null ? tx.getPaidAt() : tx.getCreatedAt();
                responses.add(CurrentEnrollmentResponse.builder()
                        .enrollmentDate(enrollmentDate)
                        .courseTitle(tx.getBatch().getTitle())
                        .price(tx.getAmount())
                        .build());
            }

            // Free batch enrollments (no paid transaction)
            List<BatchEnrollment> batchEnrollments = batchEnrollmentRepository
                    .findByMemberId(student.getId(), Pageable.unpaged())
                    .getContent();

            for (BatchEnrollment be : batchEnrollments) {
                if (!paidBatchIds.contains(be.getBatch().getId())) {
                    responses.add(CurrentEnrollmentResponse.builder()
                            .enrollmentDate(be.getEnrolledAt())
                            .courseTitle(be.getBatch().getTitle())
                            .price(BigDecimal.ZERO)
                            .build());
                }
            }
        } else {
            // Paid course enrollments from transactions
            List<Transaction> courseTransactions = transactionRepository
                    .findByStudentIdAndStatus(student.getId(), TransactionStatus.PAID, Pageable.unpaged())
                    .getContent()
                    .stream()
                    .filter(t -> t.getCourse() != null)
                    .collect(Collectors.toList());

            Set<UUID> paidCourseIds = courseTransactions.stream()
                    .map(t -> t.getCourse().getId())
                    .collect(Collectors.toCollection(HashSet::new));

            for (Transaction tx : courseTransactions) {
                OffsetDateTime enrollmentDate = tx.getPaidAt() != null ? tx.getPaidAt() : tx.getCreatedAt();
                responses.add(CurrentEnrollmentResponse.builder()
                        .enrollmentDate(enrollmentDate)
                        .courseTitle(tx.getCourse().getTitle())
                        .price(tx.getAmount())
                        .build());
            }

            // Free course enrollments (no paid transaction)
            List<Enrollment> enrollments = enrollmentRepository.findByMemberId(student.getId());
            for (Enrollment enrollment : enrollments) {
                if (!paidCourseIds.contains(enrollment.getCourse().getId())) {
                    responses.add(CurrentEnrollmentResponse.builder()
                            .enrollmentDate(enrollment.getCreation())
                            .courseTitle(enrollment.getCourse().getTitle())
                            .price(BigDecimal.ZERO)
                            .build());
                }
            }
        }

        // Sort by enrollmentDate desc
        responses.sort(Comparator.comparing(CurrentEnrollmentResponse::getEnrollmentDate).reversed());

        // Manual paging
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, responses.size());
        List<CurrentEnrollmentResponse> pageContent = fromIndex >= responses.size()
                ? new ArrayList<>()
                : responses.subList(fromIndex, toIndex);

        Pageable pageable = PageRequest.of(page, size, Sort.by("enrollmentDate").descending());
        return new PageImpl<>(pageContent, pageable, responses.size());
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
    public Page<BatchEnrollmentResponse> getBatchEnrollments(UUID batchId, Pageable pageable) {
        String currentUserEmail = getCurrentUserEmail();
        log.info("Getting enrollments for batch {} by user {}", batchId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        boolean isInstructor = batch.getInstructors().stream()
                .anyMatch(bi -> bi.getInstructor().getId().equals(currentUser.getId()));

        boolean isEnrolledStudent = batchEnrollmentRepository.existsByUserIdAndBatchId(currentUser.getId(), batchId);

        if (!isInstructor && !isEnrolledStudent) {
            throw new RuntimeException("You are not authorized to view enrollments for this batch");
        }

        return batchEnrollmentRepository.findByBatchId(batchId, pageable)
                .map(enrollment -> BatchEnrollmentResponse.builder()
                        .id(enrollment.getId())
                        .userId(enrollment.getUser().getId())
                        .fullName(enrollment.getUser().getFullName())
                        .email(enrollment.getUser().getEmail())
                        .enrolledAt(enrollment.getEnrolledAt())
                        .build());
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
