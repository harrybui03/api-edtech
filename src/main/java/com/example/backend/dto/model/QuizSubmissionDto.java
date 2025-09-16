package com.example.backend.dto.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class QuizSubmissionDto {
    private UUID id;
    private UUID quizId;
    private UUID memberId;
    private UUID courseId;
    private Integer score;
    private Integer scoreOutOf;
    private Integer percentage;
    private Integer passingPercentage;
    private String result; // JSON string containing detailed results
    private OffsetDateTime creation;
    private Boolean passed;
}
