package com.example.backend.mapper;

import com.example.backend.constant.CourseProgressStatus;
import com.example.backend.dto.response.progress.CourseProgressResponse;
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
        
        List<CourseProgressResponse.ChapterProgressResponse> chapterResponses = chapters.stream()
                .map(chapter -> {
                    List<Lesson> chapterLessons = lessonsByChapter.getOrDefault(chapter.getId(), List.of());
                    
                    List<CourseProgressResponse.LessonProgressResponse> lessonResponses = chapterLessons.stream()
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
        
        return CourseProgressResponse.builder()
                .courseId(courseId)
                .courseTitle(enrollment.getCourse().getTitle())
                .overallProgress(enrollment.getProgress())
                .completedLessons(completedLessons)
                .totalLessons(totalLessons)
                .currentLessonId(enrollment.getCurrentLesson() != null ? enrollment.getCurrentLesson().getId() : null)
                .currentLessonTitle(enrollment.getCurrentLesson() != null ? enrollment.getCurrentLesson().getTitle() : null)
                .chapters(chapterResponses)
                .build();
    }
}
