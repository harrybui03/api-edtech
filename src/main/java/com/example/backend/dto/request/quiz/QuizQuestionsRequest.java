package com.example.backend.dto.request.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class QuizQuestionsRequest {
    @NotEmpty(message = "Questions list cannot be empty")
    @Valid
    private List<QuizQuestionRequest> questions;
}
