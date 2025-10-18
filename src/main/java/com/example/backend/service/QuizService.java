package com.example.backend.service;

import com.example.backend.dto.model.QuizDto;
import com.example.backend.dto.model.QuizQuestionDto;
import com.example.backend.dto.request.quiz.QuizRequest;
import com.example.backend.dto.request.quiz.QuizQuestionRequest;
import com.example.backend.dto.request.quiz.QuizSubmissionRequest;
import com.example.backend.dto.response.quiz.QuizResponse;
import com.example.backend.dto.response.quiz.QuizSubmissionResponse;
import com.example.backend.entity.*;
import com.example.backend.excecption.ForbiddenException;
import com.example.backend.excecption.ResourceNotFoundException;
import com.example.backend.mapper.QuizMapper;
import com.example.backend.mapper.QuizQuestionMapper;
import com.example.backend.mapper.QuizSubmissionMapper;
import com.example.backend.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public QuizDto createQuiz(QuizRequest request) {
        Quiz quiz = QuizMapper.toEntity(request);
        quiz.setModifiedBy(getCurrentUserId(getCurrentUserEmail()));
        Quiz savedQuiz = quizRepository.save(quiz);

        return QuizMapper.toDto(savedQuiz);
    }

    @Transactional
    public QuizDto updateQuiz(UUID quizId, QuizRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        QuizMapper.updateEntityFromRequest(request, quiz);
        quiz.setModifiedBy(getCurrentUserId(getCurrentUserEmail()));

        // Recalculate total marks
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByCreation(quizId);
        int totalMarks = questions.stream().mapToInt(QuizQuestion::getMarks).sum();
        quiz.setTotalMarks(totalMarks);

        Quiz updatedQuiz = quizRepository.save(quiz);
        return QuizMapper.toDto(updatedQuiz);
    }

    @Transactional
    public void deleteQuiz(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        // Delete related submissions and questions (handled by cascade if configured)
        quizRepository.delete(quiz);
    }

    @Transactional
    public QuizDto addQuestionsToQuiz(UUID quizId, List<QuizQuestionRequest> requests) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        // Process all questions in batch
        List<QuizQuestion> questions = new ArrayList<>();
        for (QuizQuestionRequest request : requests) {
            QuizQuestion question = QuizQuestionMapper.toEntity(request);
            question.setQuiz(quiz);
            questions.add(question);
        }
        
        quizQuestionRepository.saveAll(questions);

        // Update total marks
        List<QuizQuestion> allQuestions = quizQuestionRepository.findByQuizIdOrderByCreation(quizId);
        int totalMarks = allQuestions.stream().mapToInt(QuizQuestion::getMarks).sum();
        quiz.setTotalMarks(totalMarks);
        quiz.setModifiedBy(getCurrentUserId(getCurrentUserEmail()));
        quizRepository.save(quiz);

        return QuizMapper.toDto(quiz);
    }

    @Transactional
    public void deleteQuestion(UUID questionId) {
        QuizQuestion question = quizQuestionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        Quiz quiz = question.getQuiz();

        // Recalculate total marks before deleting
        int marksOfDeletedQuestion = question.getMarks();
        quiz.setTotalMarks(quiz.getTotalMarks() - marksOfDeletedQuestion);
        quiz.setModifiedBy(getCurrentUserId(getCurrentUserEmail()));

        quizQuestionRepository.delete(question);
        quizRepository.save(quiz);
    }


    @Transactional
    public void updateQuestion(UUID questionId, QuizQuestionRequest request) {
        QuizQuestion question = quizQuestionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        QuizQuestionMapper.updateEntityFromRequest(request, question);
        quizQuestionRepository.save(question);

        // Update total marks in quiz
        Quiz quiz = question.getQuiz();
        List<QuizQuestion> allQuestions = quizQuestionRepository.findByQuizIdOrderByCreation(quiz.getId());
        int totalMarks = allQuestions.stream().mapToInt(QuizQuestion::getMarks).sum();
        quiz.setTotalMarks(totalMarks);
        quiz.setModifiedBy(getCurrentUserId(getCurrentUserEmail()));
        quizRepository.save(quiz);
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuizForStudent(UUID quizId, String userEmail) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get questions (without correct answers for students)
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByCreation(quizId);

        // Get user's attempt count
        int userAttempts = quizSubmissionRepository.countByQuizIdAndMemberId(quizId, user.getId());

        return QuizMapper.toResponse(quiz, questions, userAttempts);
    }

    @Transactional(readOnly = true)
    public List<QuizQuestionDto> getQuestionsByQuizId(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));

        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByCreation(quizId);
        return questions.stream()
                .map(QuizQuestionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public QuizSubmissionResponse submitQuiz(UUID quizId, QuizSubmissionRequest request, String userEmail) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get questions
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByCreation(quizId);

        // Calculate score
        Map<String, Object> detailedResult = calculateScore(questions, request.getAnswers(), quiz);
        int score = (Integer) detailedResult.get("score");
        int percentage = (Integer) detailedResult.get("percentage");

        // Create submission
        QuizSubmission submission = new QuizSubmission();
        submission.setQuiz(quiz);
        submission.setMember(user);
        submission.setScore(score);
        submission.setPercentage(percentage);

        try {
            submission.setResult(objectMapper.writeValueAsString(detailedResult));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing quiz result", e);
        }

        QuizSubmission savedSubmission = quizSubmissionRepository.save(submission);
        return QuizSubmissionMapper.toResponse(savedSubmission);
    }

    @Transactional(readOnly = true)
    public QuizSubmissionResponse getSubmissionResult(UUID submissionId, String userEmail) {
        QuizSubmission submission = quizSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        // Allow access only to the student who submitted
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isOwner = submission.getMember().getId().equals(user.getId());
        if (!isOwner) {
            throw new ForbiddenException("Access denied to this submission");
        }

        return QuizSubmissionMapper.toResponse(submission);
    }

    @Transactional(readOnly = true)
    public List<QuizSubmissionResponse> getCourseQuizSubmissions(UUID courseId) {
        // find lessons by course to derive quiz ids
        List<Lesson> lessons = lessonRepository.findByCourseId(courseId);
        if (lessons.isEmpty()) {
            return Collections.emptyList();
        }

        Set<UUID> quizIds = new HashSet<>();
        for (Lesson lesson : lessons) {
            Quiz quiz = lesson.getQuiz();
            if (quiz != null && quiz.getId() != null) {
                quizIds.add(quiz.getId());
            }
        }

        if (quizIds.isEmpty()) {
            return Collections.emptyList();
        }

        // fetch submissions for all quizzes in this course
        List<QuizSubmission> submissions = new ArrayList<>();
        for (UUID quizId : quizIds) {
            submissions.addAll(quizSubmissionRepository.findByQuizIdOrderByCreationDesc(quizId));
        }

        List<QuizSubmissionResponse> responses = new ArrayList<>(submissions.size());
        for (QuizSubmission submission : submissions) {
            responses.add(QuizSubmissionMapper.toResponse(submission));
        }
        return responses;
    }

    private Map<String, Object> calculateScore(List<QuizQuestion> questions, Map<UUID, String> answers, Quiz quiz) {
        Map<String, Object> result = new HashMap<>();
        Map<UUID, Map<String, Object>> questionResults = new HashMap<>();

        int totalScore = 0;
        int totalPossibleMarks = questions.stream().mapToInt(QuizQuestion::getMarks).sum();

        for (QuizQuestion question : questions) {
            Map<String, Object> questionResult = new HashMap<>();
            String userAnswer = answers.get(question.getId());
            String correctAnswer = question.getCorrectAnswer();

            boolean isCorrect = false;
            int marksAwarded = 0;

            if (userAnswer != null && userAnswer.trim().equalsIgnoreCase(correctAnswer != null ? correctAnswer.trim() : null)) {
                isCorrect = true;
                marksAwarded = question.getMarks();
                totalScore += marksAwarded;
            }

            questionResult.put("questionId", question.getId());
            questionResult.put("question", question.getQuestion());
            questionResult.put("userAnswer", userAnswer);
            questionResult.put("correctAnswer", correctAnswer);
            questionResult.put("isCorrect", isCorrect);
            questionResult.put("marksAwarded", marksAwarded);
            questionResult.put("maxMarks", question.getMarks());

            questionResults.put(question.getId(), questionResult);
        }

        // Ensure score doesn't go below 0
        totalScore = Math.max(0, totalScore);
        
        int percentage = totalPossibleMarks > 0 ? (totalScore * 100) / totalPossibleMarks : 0;

        result.put("score", totalScore);
        result.put("totalPossibleMarks", totalPossibleMarks);
        result.put("percentage", percentage);
        result.put("questionResults", questionResults);
        // No pass/fail flag in simplified model

        return result;
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    
    private UUID getCurrentUserId(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void checkCourseOwnership(Course course) {
        User currentUser = userRepository.findByEmail(getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isOwner = course.getInstructors().stream()
                .anyMatch(instructor -> instructor.getUser().getId().equals(currentUser.getId()));

        if (!isOwner) {
            throw new ForbiddenException("You are not an instructor for this course and cannot modify its content.");
        }
    }
}
