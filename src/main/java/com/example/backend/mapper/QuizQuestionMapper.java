package com.example.backend.mapper;

import com.example.backend.dto.model.QuizQuestionDto;
import com.example.backend.dto.request.quiz.QuizQuestionRequest;
import com.example.backend.entity.QuizQuestion;

public final class QuizQuestionMapper {

    public static QuizQuestionDto toDto(QuizQuestion question) {
        if (question == null) {
            return null;
        }
        QuizQuestionDto dto = new QuizQuestionDto();
        dto.setId(question.getId());
        dto.setQuizId(question.getQuiz() != null ? question.getQuiz().getId() : null);
        dto.setQuestion(question.getQuestion());
        dto.setType(question.getType());
        dto.setOptions(question.getOptions());
        dto.setCorrectAnswer(question.getCorrectAnswer());
        dto.setMarks(question.getMarks());
        dto.setCreation(question.getCreation());
        return dto;
    }

    public static QuizQuestionDto toDtoWithoutAnswer(QuizQuestion question) {
        if (question == null) {
            return null;
        }
        QuizQuestionDto dto = new QuizQuestionDto();
        dto.setId(question.getId());
        dto.setQuizId(question.getQuiz() != null ? question.getQuiz().getId() : null);
        dto.setQuestion(question.getQuestion());
        dto.setType(question.getType());
        dto.setOptions(question.getOptions());
        dto.setMarks(question.getMarks());
        dto.setCreation(question.getCreation());
        // Note: correctAnswer is intentionally not set for student view
        return dto;
    }

    public static QuizQuestion toEntity(QuizQuestionRequest request) {
        if (request == null) {
            return null;
        }
        QuizQuestion question = new QuizQuestion();
        question.setQuestion(request.getQuestion());
        question.setType(request.getType());
        question.setOptions(request.getOptions());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setMarks(request.getMarks());
        return question;
    }

    public static void updateEntityFromRequest(QuizQuestionRequest request, QuizQuestion question) {
        if (request == null || question == null) {
            return;
        }
        question.setQuestion(request.getQuestion());
        question.setType(request.getType());
        question.setOptions(request.getOptions());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setMarks(request.getMarks());
    }
}
