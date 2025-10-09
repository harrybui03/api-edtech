package com.example.backend.mapper;

import com.example.backend.dto.model.QuizDto;
import com.example.backend.dto.model.QuizQuestionDto;
import com.example.backend.dto.request.quiz.QuizRequest;
import com.example.backend.dto.response.quiz.QuizResponse;
import com.example.backend.entity.Quiz;
import com.example.backend.entity.QuizQuestion;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class QuizMapper {

    public static QuizDto toDto(Quiz quiz) {
        if (quiz == null) {
            return null;
        }
        QuizDto dto = new QuizDto();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setShowAnswers(quiz.getShowAnswers());
        dto.setShowSubmissionHistory(quiz.getShowSubmissionHistory());
        dto.setTotalMarks(quiz.getTotalMarks());
        dto.setCreation(quiz.getCreation());
        dto.setModified(quiz.getModified());
        dto.setModifiedBy(quiz.getModifiedBy());
        return dto;
    }

    public static QuizResponse toResponse(Quiz quiz, List<QuizQuestion> questions, Integer userAttempts) {
        if (quiz == null) {
            return null;
        }
        QuizResponse response = new QuizResponse();
        response.setId(quiz.getId());
        response.setTitle(quiz.getTitle());
        response.setShowAnswers(quiz.getShowAnswers());
        response.setShowSubmissionHistory(quiz.getShowSubmissionHistory());
        response.setTotalMarks(quiz.getTotalMarks());
        response.setCreation(quiz.getCreation());
        response.setModified(quiz.getModified());
        response.setModifiedBy(quiz.getModifiedBy());
        response.setQuestions(toQuestionDtoList(questions));
        response.setUserAttempts(userAttempts);
        return response;
    }

    public static Quiz toEntity(QuizRequest request) {
        if (request == null) {
            return null;
        }
        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle());
        quiz.setShowAnswers(request.getShowAnswers());
        quiz.setShowSubmissionHistory(request.getShowSubmissionHistory());
        return quiz;
    }

    public static void updateEntityFromRequest(QuizRequest request, Quiz quiz) {
        if (request == null || quiz == null) {
            return;
        }
        quiz.setTitle(request.getTitle());
        quiz.setShowAnswers(request.getShowAnswers());
        quiz.setShowSubmissionHistory(request.getShowSubmissionHistory());
    }

    private static List<QuizQuestionDto> toQuestionDtoList(List<QuizQuestion> questions) {
        if (questions == null || questions.isEmpty()) {
            return Collections.emptyList();
        }
        return questions.stream()
                .map(QuizQuestionMapper::toDto)
                .collect(Collectors.toList());
    }
}
