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
                .currentLessonId(enrollment.getCurrentLesson() != null ? enrollment.getCurrentLesson().getId() : null)
                .currentLessonTitle(enrollment.getCurrentLesson() != null ? enrollment.getCurrentLesson().getTitle() : null)
                .currentLessonSlug(enrollment.getCurrentLesson() != null ? enrollment.getCurrentLesson().getSlug() : null)
                .chapters(chapterResponses)
                .build();
    }
}
