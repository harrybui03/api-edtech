package com.example.backend.dto.request.course;

import lombok.Data;

import java.util.UUID;

@Data
public class LessonRequest {
    private String title;
    private String content;
    private String videoUrl;
    private String fileUrl;
    private UUID quizId;
}