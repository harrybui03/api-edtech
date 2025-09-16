package com.example.backend.dto.response.quiz;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class QuizSubmissionResponse {
    private UUID id;
    private UUID quizId;
    private String quizTitle;
    private UUID memberId;
    private String memberName;
    private UUID courseId;
    private String courseTitle;
    private Integer score;
    private Integer scoreOutOf;
    private Integer percentage;
    private Integer passingPercentage;
    private Boolean passed;
    private String result; // JSON string containing detailed results
    private OffsetDateTime creation;
}
