package com.example.backend.dto.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class QuizDto {
    private UUID id;
    private String title;
    private Boolean showAnswers;
    private Boolean showSubmissionHistory;
    private Integer totalMarks;
    private OffsetDateTime creation;
    private OffsetDateTime modified;
    private UUID modifiedBy;
}
