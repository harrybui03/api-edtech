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
        dto.setLessonId(quiz.getLesson() != null ? quiz.getLesson().getId() : null);
        dto.setCourseId(quiz.getCourse() != null ? quiz.getCourse().getId() : null);
        dto.setMaxAttempts(quiz.getMaxAttempts());
        dto.setShowAnswers(quiz.getShowAnswers());
        dto.setShowSubmissionHistory(quiz.getShowSubmissionHistory());
        dto.setTotalMarks(quiz.getTotalMarks());
        dto.setPassingPercentage(quiz.getPassingPercentage());
        dto.setDuration(quiz.getDuration());
        dto.setShuffleQuestions(quiz.getShuffleQuestions());
        dto.setLimitQuestionsTo(quiz.getLimitQuestionsTo());
        dto.setEnableNegativeMarking(quiz.getEnableNegativeMarking());
        dto.setMarksToCut(quiz.getMarksToCut());
        dto.setCreation(quiz.getCreation());
        dto.setModified(quiz.getModified());
        return dto;
    }

    public static QuizResponse toResponse(Quiz quiz, List<QuizQuestion> questions, Integer userAttempts) {
        if (quiz == null) {
            return null;
        }
        QuizResponse response = new QuizResponse();
        response.setId(quiz.getId());
        response.setTitle(quiz.getTitle());
        response.setLessonId(quiz.getLesson() != null ? quiz.getLesson().getId() : null);
        response.setCourseId(quiz.getCourse() != null ? quiz.getCourse().getId() : null);
        response.setMaxAttempts(quiz.getMaxAttempts());
        response.setShowAnswers(quiz.getShowAnswers());
        response.setShowSubmissionHistory(quiz.getShowSubmissionHistory());
        response.setTotalMarks(quiz.getTotalMarks());
        response.setPassingPercentage(quiz.getPassingPercentage());
        response.setDuration(quiz.getDuration());
        response.setShuffleQuestions(quiz.getShuffleQuestions());
        response.setLimitQuestionsTo(quiz.getLimitQuestionsTo());
        response.setEnableNegativeMarking(quiz.getEnableNegativeMarking());
        response.setMarksToCut(quiz.getMarksToCut());
        response.setCreation(quiz.getCreation());
        response.setModified(quiz.getModified());
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
        quiz.setMaxAttempts(request.getMaxAttempts());
        quiz.setShowAnswers(request.getShowAnswers());
        quiz.setShowSubmissionHistory(request.getShowSubmissionHistory());
        quiz.setPassingPercentage(request.getPassingPercentage());
        quiz.setDuration(request.getDuration());
        quiz.setShuffleQuestions(request.getShuffleQuestions());
        quiz.setLimitQuestionsTo(request.getLimitQuestionsTo());
        quiz.setEnableNegativeMarking(request.getEnableNegativeMarking());
        quiz.setMarksToCut(request.getMarksToCut());
        return quiz;
    }

    public static void updateEntityFromRequest(QuizRequest request, Quiz quiz) {
        if (request == null || quiz == null) {
            return;
        }
        quiz.setTitle(request.getTitle());
        quiz.setMaxAttempts(request.getMaxAttempts());
        quiz.setShowAnswers(request.getShowAnswers());
        quiz.setShowSubmissionHistory(request.getShowSubmissionHistory());
        quiz.setPassingPercentage(request.getPassingPercentage());
        quiz.setDuration(request.getDuration());
        quiz.setShuffleQuestions(request.getShuffleQuestions());
        quiz.setLimitQuestionsTo(request.getLimitQuestionsTo());
        quiz.setEnableNegativeMarking(request.getEnableNegativeMarking());
        quiz.setMarksToCut(request.getMarksToCut());
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
