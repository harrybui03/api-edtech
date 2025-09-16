package com.example.backend.dto.request.quiz;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class QuizSubmissionRequest {
    @NotNull(message = "Answers are required")
    private Map<UUID, String> answers; // Map of questionId -> answer
}
