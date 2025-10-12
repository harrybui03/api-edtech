package com.example.backend.service;

import com.example.backend.constant.CourseProgressStatus;
import com.example.backend.dto.response.progress.CourseProgressResponse;
import com.example.backend.entity.*;
import com.example.backend.mapper.ProgressMapper;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProgressTrackingService {
    
    private final CourseProgressRepository courseProgressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final ChapterRepository chapterRepository;
    private final CourseRepository courseRepository;
    private final ProgressMapper progressMapper;
    
    public void markLessonCompleted(String studentEmail, UUID lessonId) {
        log.info("Marking lesson {} as completed for student {}", lessonId, studentEmail);
        
        // Validate student and lesson
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        
        // Check if student is enrolled in the course
        if (!enrollmentRepository.existsByMemberIdAndCourseId(student.getId(), lesson.getCourse().getId())) {
            throw new RuntimeException("Student is not enrolled in this course");
        }
        
        // Check if progress already exists
        CourseProgress progress = courseProgressRepository
                .findByMemberIdAndLessonId(student.getId(), lessonId)
                .orElse(new CourseProgress());
        
        // Update or create progress
        if (progress.getId() == null) {
            progress.setMember(student);
            progress.setLesson(lesson);
            progress.setChapter(lesson.getChapter());
            progress.setCourse(lesson.getCourse());
        }
        
        progress.setStatus(CourseProgressStatus.COMPLETE);
        courseProgressRepository.save(progress);
        
        // Update enrollment progress
        updateEnrollmentProgress(student.getId(), lesson.getCourse().getId());
        
        log.info("Successfully marked lesson {} as completed for student {}", lessonId, studentEmail);
    }
    
    @Transactional(readOnly = true)
    public CourseProgressResponse getCourseProgress(String studentEmail, UUID courseId) {
        log.info("Getting course progress for student {} in course {}", studentEmail, courseId);
        
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        // Validate enrollment
        Enrollment enrollment = enrollmentRepository.findByMemberIdAndCourseId(student.getId(), courseId)
                .orElseThrow(() -> new RuntimeException("Student is not enrolled in this course"));
        
        // Get all progress for this course
        List<CourseProgress> progressList = courseProgressRepository.findByMemberIdAndCourseId(student.getId(), courseId);
        Map<UUID, CourseProgress> progressMap = progressList.stream()
                .collect(Collectors.toMap(cp -> cp.getLesson().getId(), cp -> cp));
        
        // Get all chapters and lessons for this course
        List<Chapter> chapters = chapterRepository.findByCourseIdOrderByCreation(courseId);
        
        // Group lessons by chapter
        Map<UUID, List<Lesson>> lessonsByChapter = chapters.stream()
                .collect(Collectors.toMap(
                        Chapter::getId,
                        chapter -> lessonRepository.findByChapterIdOrderByPosition(chapter.getId())
                ));
        
        // Calculate completed and total lessons
        long completedLessons = courseProgressRepository.countCompletedLessons(student.getId(), courseId);
        long totalLessons = courseProgressRepository.countTotalLessons(courseId);
        
        return progressMapper.mapToCourseProgressResponse(
                courseId,
                enrollment,
                chapters,
                progressMap,
                lessonsByChapter,
                (int) completedLessons,
                (int) totalLessons
        );
    }
    
    public void markLessonCompletedBySlug(String studentEmail, String lessonSlug) {
        log.info("Marking lesson {} as completed for student {}", lessonSlug, studentEmail);
        
        // Validate student and lesson
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        Lesson lesson = lessonRepository.findBySlug(lessonSlug)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        
        // Check if student is enrolled in the course
        if (!enrollmentRepository.existsByMemberIdAndCourseId(student.getId(), lesson.getCourse().getId())) {
            throw new RuntimeException("Student is not enrolled in this course");
        }
        
        // Check if progress already exists
        CourseProgress progress = courseProgressRepository
                .findByMemberIdAndLessonId(student.getId(), lesson.getId())
                .orElse(new CourseProgress());
        
        // Update or create progress
        if (progress.getId() == null) {
            progress.setMember(student);
            progress.setLesson(lesson);
            progress.setChapter(lesson.getChapter());
            progress.setCourse(lesson.getCourse());
        }
        
        progress.setStatus(CourseProgressStatus.COMPLETE);
        courseProgressRepository.save(progress);
        
        // Update enrollment progress
        updateEnrollmentProgress(student.getId(), lesson.getCourse().getId());
        
        log.info("Successfully marked lesson {} as completed for student {}", lessonSlug, studentEmail);
    }
    
    @Transactional(readOnly = true)
    public CourseProgressResponse getCourseProgressBySlug(String studentEmail, String courseSlug) {
        log.info("Getting course progress for student {} in course slug {}", studentEmail, courseSlug);
        
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        Course course = courseRepository.findBySlug(courseSlug)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Validate enrollment
        Enrollment enrollment = enrollmentRepository.findByMemberIdAndCourseId(student.getId(), course.getId())
                .orElseThrow(() -> new RuntimeException("Student is not enrolled in this course"));
        
        // Get all progress for this course
        List<CourseProgress> progressList = courseProgressRepository.findByMemberIdAndCourseId(student.getId(), course.getId());
        Map<UUID, CourseProgress> progressMap = progressList.stream()
                .collect(Collectors.toMap(cp -> cp.getLesson().getId(), cp -> cp));
        
        // Get all chapters and lessons for this course
        List<Chapter> chapters = chapterRepository.findByCourseIdOrderByCreation(course.getId());
        
        // Group lessons by chapter
        Map<UUID, List<Lesson>> lessonsByChapter = chapters.stream()
                .collect(Collectors.toMap(
                        Chapter::getId,
                        chapter -> lessonRepository.findByChapterIdOrderByPosition(chapter.getId())
                ));
        
        // Calculate completed and total lessons
        long completedLessons = courseProgressRepository.countCompletedLessons(student.getId(), course.getId());
        long totalLessons = courseProgressRepository.countTotalLessons(course.getId());
        
        return progressMapper.mapToCourseProgressResponse(
                course.getId(),
                enrollment,
                chapters,
                progressMap,
                lessonsByChapter,
                (int) completedLessons,
                (int) totalLessons
        );
    }

    private void updateEnrollmentProgress(UUID studentId, UUID courseId) {
        Enrollment enrollment = enrollmentRepository.findByMemberIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        
        long completedLessons = courseProgressRepository.countCompletedLessons(studentId, courseId);
        long totalLessons = courseProgressRepository.countTotalLessons(courseId);
        
        if (totalLessons > 0) {
            BigDecimal progress = BigDecimal.valueOf(completedLessons)
                    .divide(BigDecimal.valueOf(totalLessons), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            
            enrollment.setProgress(progress);
        }
        
        // Set current lesson to the next incomplete lesson
        List<Lesson> incompleteLessons = lessonRepository.findIncompleteLessonsByCourseIdAndMemberId(courseId, studentId);
        if (!incompleteLessons.isEmpty()) {
            enrollment.setCurrentLesson(incompleteLessons.get(0));
        } else {
            // All lessons completed, set current lesson to null
            enrollment.setCurrentLesson(null);
        }
        
        enrollmentRepository.save(enrollment);
    }
}
