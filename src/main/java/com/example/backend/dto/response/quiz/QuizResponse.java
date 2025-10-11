package com.example.backend.dto.response.quiz;

import com.example.backend.dto.model.QuizQuestionDto;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class QuizResponse {
    private UUID id;
    private String title;
    private Boolean showAnswers;
    private Boolean showSubmissionHistory;
    private Integer totalMarks;
    private OffsetDateTime creation;
    private OffsetDateTime modified;
    private UUID modifiedBy;
    private List<QuizQuestionDto> questions;
    private Integer userAttempts; // Number of attempts by current user
}
