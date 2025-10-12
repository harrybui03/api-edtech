package com.example.backend.dto.request.quiz;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuizRequest {
    @NotBlank(message = "Quiz title is required")
    private String title;
    
    private Boolean showAnswers = true;
    private Boolean showSubmissionHistory = false;
}
