package com.example.backend.dto.response.progress;

import com.example.backend.constant.CourseProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgressResponse {
    private UUID lessonId;
    private String lessonTitle;
    private CourseProgressStatus status;
    private OffsetDateTime completedAt;
    private Integer duration;
}
