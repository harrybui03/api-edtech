package com.example.backend.dto.model;

import lombok.Data;
import java.util.UUID;

@Data
public class LessonDto {
    private UUID id;
    private String title;
    private String slug;
    private String content;
    private String videoUrl;
    private String fileUrl;
    private Integer position;
    private QuizDto quizDto;
}