package com.example.backend.dto.request.quiz;

import com.example.backend.constant.QuizQuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class QuizQuestionRequest {
    @NotBlank(message = "Question is required")
    private String question;
    
    @NotNull(message = "Question type is required")
    private QuizQuestionType type;
    
    private String options; // JSON string for multiple choice options
    
    @NotBlank(message = "Correct answer is required")
    private String correctAnswer;
    
    @NotNull(message = "Marks is required")
    @Min(value = 1, message = "Marks must be at least 1")
    private Integer marks = 1;
}
