package com.example.backend.dto.model;

import com.example.backend.constant.QuizQuestionType;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class QuizQuestionDto {
    private UUID id;
    private UUID quizId;
    private String question;
    private QuizQuestionType type;
    private String options; // JSON string for multiple choice options
    private String correctAnswer;
    private Integer marks;
    private OffsetDateTime creation;
}
