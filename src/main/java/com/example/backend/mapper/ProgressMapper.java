package com.example.backend.mapper;

import com.example.backend.constant.CourseProgressStatus;
import com.example.backend.dto.response.progress.CourseProgressResponse;
import com.example.backend.dto.response.progress.ChapterProgressResponse;
import com.example.backend.dto.response.progress.LessonProgressResponse;
import com.example.backend.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ProgressMapper {
    
    public CourseProgressResponse mapToCourseProgressResponse(
            UUID courseId,
            Enrollment enrollment,
            List<Chapter> chapters,
            Map<UUID, CourseProgress> progressMap,
            Map<UUID, List<Lesson>> lessonsByChapter,
            int completedLessons,
            int totalLessons) {
        
        List<ChapterProgressResponse> chapterResponses = chapters.stream()
                .map(chapter -> {
                    List<Lesson> chapterLessons = lessonsByChapter.getOrDefault(chapter.getId(), List.of());
                    
                    List<LessonProgressResponse> lessonResponses = chapterLessons.stream()
                            .map(lesson -> {
                                CourseProgress progress = progressMap.get(lesson.getId());
                                return LessonProgressResponse.builder()
                                        .lessonId(lesson.getId())
                                        .lessonTitle(lesson.getTitle())
                                        .status(progress != null ? progress.getStatus() : CourseProgressStatus.INCOMPLETE)
                                        .completedAt(progress != null ? progress.getModified() : null)
                                        .build();
                            })
                            .collect(Collectors.toList());
                    
                    return ChapterProgressResponse.builder()
                            .chapterId(chapter.getId())
                            .chapterTitle(chapter.getTitle())
                            .lessons(lessonResponses)
                            .build();
                })
                .collect(Collectors.toList());
        
        return CourseProgressResponse.builder()
                .courseId(courseId)
                .courseTitle(enrollment.getCourse().getTitle())
                .courseSlug(enrollment.getCourse().getSlug())
                .overallProgress(enrollment.getProgress())
                .completedLessons(completedLessons)
                .totalLessons(totalLessons)
                .currentLessonId(resolveCurrentLessonId(enrollment, chapters, lessonsByChapter))
                .currentLessonTitle(resolveCurrentLessonTitle(enrollment, chapters, lessonsByChapter))
                .currentLessonSlug(resolveCurrentLessonSlug(enrollment, chapters, lessonsByChapter))
                .chapters(chapterResponses)
                .build();
    }

    private UUID resolveCurrentLessonId(Enrollment enrollment, List<Chapter> chapters, Map<UUID, List<Lesson>> lessonsByChapter) {
        Lesson current = enrollment.getCurrentLesson();
        if (current != null) return current.getId();
        Lesson fallback = getFirstOrLastLesson(chapters, lessonsByChapter, enrollment);
        return fallback != null ? fallback.getId() : null;
    }

    private String resolveCurrentLessonTitle(Enrollment enrollment, List<Chapter> chapters, Map<UUID, List<Lesson>> lessonsByChapter) {
        Lesson current = enrollment.getCurrentLesson();
        if (current != null) return current.getTitle();
        Lesson fallback = getFirstOrLastLesson(chapters, lessonsByChapter, enrollment);
        return fallback != null ? fallback.getTitle() : null;
    }

    private String resolveCurrentLessonSlug(Enrollment enrollment, List<Chapter> chapters, Map<UUID, List<Lesson>> lessonsByChapter) {
        Lesson current = enrollment.getCurrentLesson();
        if (current != null) return current.getSlug();
        Lesson fallback = getFirstOrLastLesson(chapters, lessonsByChapter, enrollment);
        return fallback != null ? fallback.getSlug() : null;
    }

    // If enrollment has no current lesson: return first lesson if progress is zero (start),
    // or last lesson if progress is 100% (completed). Otherwise null.
    private Lesson getFirstOrLastLesson(List<Chapter> chapters, Map<UUID, List<Lesson>> lessonsByChapter, Enrollment enrollment) {
        if (chapters == null || chapters.isEmpty()) return null;
        boolean isAtStart = enrollment.getProgress() == null || enrollment.getProgress().signum() == 0;
        boolean isCompleted = enrollment.getProgress() != null && enrollment.getProgress().compareTo(java.math.BigDecimal.valueOf(100)) >= 0;

        if (isAtStart) {
            for (Chapter chapter : chapters) {
                List<Lesson> lessons = lessonsByChapter.getOrDefault(chapter.getId(), List.of());
                if (!lessons.isEmpty()) return lessons.get(0);
            }
        }
        if (isCompleted) {
            for (int i = chapters.size() - 1; i >= 0; i--) {
                Chapter chapter = chapters.get(i);
                List<Lesson> lessons = lessonsByChapter.getOrDefault(chapter.getId(), List.of());
                if (!lessons.isEmpty()) return lessons.get(lessons.size() - 1);
            }
        }
        return null;
    }
}
