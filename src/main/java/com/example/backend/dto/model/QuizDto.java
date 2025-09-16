package com.example.backend.dto.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class QuizDto {
    private UUID id;
    private String title;
    private UUID lessonId;
    private UUID courseId;
    private Integer maxAttempts;
    private Boolean showAnswers;
    private Boolean showSubmissionHistory;
    private Integer totalMarks;
    private Integer passingPercentage;
    private String duration;
    private Boolean shuffleQuestions;
    private Integer limitQuestionsTo;
    private Boolean enableNegativeMarking;
    private Integer marksToCut;
    private OffsetDateTime creation;
    private OffsetDateTime modified;
    private List<QuizQuestionDto> questions;
}
