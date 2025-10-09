package com.example.backend.controller;

import com.example.backend.dto.request.quiz.QuizSubmissionRequest;
import com.example.backend.dto.response.quiz.QuizResponse;
import com.example.backend.dto.response.quiz.QuizSubmissionResponse;
import com.example.backend.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Quiz", description = "Quiz taking and submission APIs for students")
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/quizzes/{quizId}")
    @Operation(summary = "Get quiz for taking", description = "Student gets quiz details to start taking the quiz")
    public ResponseEntity<QuizResponse> getQuizForTaking(@PathVariable UUID quizId) {
        String currentUserEmail = getCurrentUserEmail();
        QuizResponse quiz = quizService.getQuizForStudent(quizId, currentUserEmail);
        return ResponseEntity.ok(quiz);
    }

    @PostMapping("/quizzes/{quizId}/submit")
    @Operation(summary = "Submit quiz", description = "Student submits their quiz answers for automatic scoring")
    public ResponseEntity<QuizSubmissionResponse> submitQuiz(
            @PathVariable UUID quizId,
            @RequestBody QuizSubmissionRequest request) {
        String currentUserEmail = getCurrentUserEmail();
        QuizSubmissionResponse submission = quizService.submitQuiz(quizId, request, currentUserEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(submission);
    }

    @GetMapping("/quiz-submissions/{submissionId}")
    @Operation(summary = "Get submission result", description = "Get detailed results of a quiz submission")
    public ResponseEntity<QuizSubmissionResponse> getSubmissionResult(@PathVariable UUID submissionId) {
        String currentUserEmail = getCurrentUserEmail();
        QuizSubmissionResponse result = quizService.getSubmissionResult(submissionId, currentUserEmail);
        return ResponseEntity.ok(result);
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
