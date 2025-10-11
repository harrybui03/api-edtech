package com.example.backend.dto.response.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgressResponse {
    private UUID courseId;
    private String courseTitle;
    private String courseSlug;
    private BigDecimal overallProgress;
    private int completedLessons;
    private int totalLessons;
    private UUID currentLessonId;
    private String currentLessonTitle;
    private String currentLessonSlug;
    private List<ChapterProgressResponse> chapters;
}
