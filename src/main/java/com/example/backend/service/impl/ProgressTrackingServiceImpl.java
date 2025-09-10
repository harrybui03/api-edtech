package com.example.backend.service.impl;

import com.example.backend.constant.CourseProgressStatus;
import com.example.backend.dto.request.progress.VideoWatchTimeRequest;
import com.example.backend.dto.response.progress.CourseProgressResponse;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import com.example.backend.service.ProgressTrackingService;
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
public class ProgressTrackingServiceImpl implements ProgressTrackingService {
    
    private final CourseProgressRepository courseProgressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final VideoWatchDurationRepository videoWatchDurationRepository;
    private final ChapterRepository chapterRepository;
    
    @Override
    public void markLessonCompleted(UUID studentId, UUID lessonId) {
        log.info("Marking lesson {} as completed for student {}", lessonId, studentId);
        
        // Validate student and lesson
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        
        // Check if student is enrolled in the course
        if (!enrollmentRepository.existsByMemberIdAndCourseId(studentId, lesson.getCourse().getId())) {
            throw new RuntimeException("Student is not enrolled in this course");
        }
        
        // Check if progress already exists
        CourseProgress progress = courseProgressRepository
                .findByMemberIdAndLessonId(studentId, lessonId)
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
        updateEnrollmentProgress(studentId, lesson.getCourse().getId());
        
        log.info("Successfully marked lesson {} as completed for student {}", lessonId, studentId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CourseProgressResponse getCourseProgress(UUID studentId, UUID courseId) {
        log.info("Getting course progress for student {} in course {}", studentId, courseId);
        
        // Validate enrollment
        Enrollment enrollment = enrollmentRepository.findByMemberIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("Student is not enrolled in this course"));
        
        // Get all progress for this course
        List<CourseProgress> progressList = courseProgressRepository.findByMemberIdAndCourseId(studentId, courseId);
        Map<UUID, CourseProgress> progressMap = progressList.stream()
                .collect(Collectors.toMap(cp -> cp.getLesson().getId(), cp -> cp));
        
        // Get all chapters and lessons for this course
        List<Chapter> chapters = chapterRepository.findByCourseIdOrderByCreation(courseId);
        
        List<CourseProgressResponse.ChapterProgressResponse> chapterResponses = chapters.stream()
                .map(chapter -> {
                    List<Lesson> lessons = lessonRepository.findByChapterIdOrderByPosition(chapter.getId());
                    
                    List<CourseProgressResponse.LessonProgressResponse> lessonResponses = lessons.stream()
                            .map(lesson -> {
                                CourseProgress progress = progressMap.get(lesson.getId());
                                return CourseProgressResponse.LessonProgressResponse.builder()
                                        .lessonId(lesson.getId())
                                        .lessonTitle(lesson.getTitle())
                                        .status(progress != null ? progress.getStatus() : CourseProgressStatus.INCOMPLETE)
                                        .completedAt(progress != null ? progress.getModified() : null)
                                        .duration(lesson.getDuration())
                                        .videoUrl(lesson.getVideoUrl())
                                        .build();
                            })
                            .collect(Collectors.toList());
                    
                    return CourseProgressResponse.ChapterProgressResponse.builder()
                            .chapterId(chapter.getId())
                            .chapterTitle(chapter.getTitle())
                            .lessons(lessonResponses)
                            .build();
                })
                .collect(Collectors.toList());
        
        // Calculate completed and total lessons
        long completedLessons = courseProgressRepository.countCompletedLessons(studentId, courseId);
        long totalLessons = courseProgressRepository.countTotalLessons(courseId);
        
        return CourseProgressResponse.builder()
                .courseId(courseId)
                .courseTitle(enrollment.getCourse().getTitle())
                .overallProgress(enrollment.getProgress())
                .completedLessons((int) completedLessons)
                .totalLessons((int) totalLessons)
                .currentLessonId(enrollment.getCurrentLesson() != null ? enrollment.getCurrentLesson().getId() : null)
                .currentLessonTitle(enrollment.getCurrentLesson() != null ? enrollment.getCurrentLesson().getTitle() : null)
                .chapters(chapterResponses)
                .build();
    }
    
    @Override
    public void recordVideoWatchTime(UUID studentId, UUID lessonId, VideoWatchTimeRequest request) {
        log.info("Recording video watch time for student {} in lesson {}", studentId, lessonId);
        
        // Validate student and lesson
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        
        // Check if student is enrolled in the course
        if (!enrollmentRepository.existsByMemberIdAndCourseId(studentId, lesson.getCourse().getId())) {
            throw new RuntimeException("Student is not enrolled in this course");
        }
        
        // Create watch duration record
        VideoWatchDuration watchDuration = VideoWatchDuration.builder()
                .member(student)
                .lesson(lesson)
                .chapter(lesson.getChapter())
                .course(lesson.getCourse())
                .source(request.getSource())
                .watchTime(request.getWatchTime())
                .build();
        
        videoWatchDurationRepository.save(watchDuration);
        
        log.info("Successfully recorded video watch time for student {} in lesson {}", studentId, lessonId);
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
            enrollmentRepository.save(enrollment);
        }
    }
}
