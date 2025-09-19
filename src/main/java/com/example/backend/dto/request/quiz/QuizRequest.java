package com.example.backend.dto.request.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.UUID;

@Data
public class QuizRequest {
    @NotBlank(message = "Quiz title is required")
    private String title;
    
    private String explanation;
    
    private UUID lessonId;
    
    @NotNull(message = "Course ID is required")
    private UUID courseId;
    
    @Min(value = 0, message = "Max attempts must be 0 or greater")
    private Integer maxAttempts = 0;
    
    private Boolean showAnswers = true;
    private Boolean showSubmissionHistory = false;
    
    @NotNull(message = "Passing percentage is required")
    @Min(value = 0, message = "Passing percentage must be between 0 and 100")
    @Max(value = 100, message = "Passing percentage must be between 0 and 100")
    private Integer passingPercentage;
    
    private String duration;
    private Boolean shuffleQuestions = false;
    private Integer limitQuestionsTo;
    private Boolean enableNegativeMarking = false;
    
    @Min(value = 1, message = "Marks to cut must be at least 1")
    private Integer marksToCut = 1;
}
