package com.example.backend.mapper;

import com.example.backend.dto.model.QuizSubmissionDto;
import com.example.backend.dto.response.quiz.QuizSubmissionResponse;
import com.example.backend.entity.QuizSubmission;

public final class QuizSubmissionMapper {

    public static QuizSubmissionDto toDto(QuizSubmission submission) {
        if (submission == null) {
            return null;
        }
        QuizSubmissionDto dto = new QuizSubmissionDto();
        dto.setId(submission.getId());
        dto.setQuizId(submission.getQuiz() != null ? submission.getQuiz().getId() : null);
        dto.setMemberId(submission.getMember() != null ? submission.getMember().getId() : null);
        dto.setCourseId(submission.getCourse() != null ? submission.getCourse().getId() : null);
        dto.setScore(submission.getScore());
        dto.setScoreOutOf(submission.getScoreOutOf());
        dto.setPercentage(submission.getPercentage());
        dto.setPassingPercentage(submission.getPassingPercentage());
        dto.setResult(submission.getResult());
        dto.setCreation(submission.getCreation());
        dto.setPassed(submission.getPercentage() >= submission.getPassingPercentage());
        return dto;
    }

    public static QuizSubmissionResponse toResponse(QuizSubmission submission) {
        if (submission == null) {
            return null;
        }
        QuizSubmissionResponse response = new QuizSubmissionResponse();
        response.setId(submission.getId());
        response.setQuizId(submission.getQuiz() != null ? submission.getQuiz().getId() : null);
        response.setQuizTitle(submission.getQuiz() != null ? submission.getQuiz().getTitle() : null);
        response.setMemberId(submission.getMember() != null ? submission.getMember().getId() : null);
        response.setMemberName(submission.getMember() != null ? submission.getMember().getFullName() : null);
        response.setCourseId(submission.getCourse() != null ? submission.getCourse().getId() : null);
        response.setCourseTitle(submission.getCourse() != null ? submission.getCourse().getTitle() : null);
        response.setScore(submission.getScore());
        response.setScoreOutOf(submission.getScoreOutOf());
        response.setPercentage(submission.getPercentage());
        response.setPassingPercentage(submission.getPassingPercentage());
        response.setPassed(submission.getPercentage() >= submission.getPassingPercentage());
        response.setResult(submission.getResult());
        response.setCreation(submission.getCreation());
        return response;
    }
}
