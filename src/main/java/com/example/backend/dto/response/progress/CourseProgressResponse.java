package com.example.backend.dto.response.progress;

import com.example.backend.constant.CourseProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgressResponse {
    private UUID courseId;
    private String courseTitle;
    private BigDecimal overallProgress;
    private int completedLessons;
    private int totalLessons;
    private UUID currentLessonId;
    private String currentLessonTitle;
    private List<ChapterProgressResponse> chapters;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterProgressResponse {
        private UUID chapterId;
        private String chapterTitle;
        private List<LessonProgressResponse> lessons;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonProgressResponse {
        private UUID lessonId;
        private String lessonTitle;
        private CourseProgressStatus status;
        private OffsetDateTime completedAt;
        private Integer duration;
        private String videoUrl;
    }
}
