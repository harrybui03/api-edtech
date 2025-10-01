package com.example.backend.service;

import com.example.backend.dto.model.QuizDto;
import com.example.backend.dto.request.quiz.QuizRequest;
import com.example.backend.dto.request.quiz.QuizQuestionRequest;
import com.example.backend.dto.request.quiz.QuizSubmissionRequest;
import com.example.backend.dto.response.quiz.QuizResponse;
import com.example.backend.dto.response.quiz.QuizSubmissionResponse;
import com.example.backend.entity.*;
import com.example.backend.excecption.ForbiddenException;
import com.example.backend.excecption.InvalidRequestDataException;
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
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public QuizDto createQuiz(QuizRequest request) {
        String instructorEmail = getCurrentUserEmail();
        
        // Verify instructor access to course
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        verifyInstructorAccess(course.getId(), instructorEmail);

        Quiz quiz = QuizMapper.toEntity(request);
        quiz.setCourse(course);

        // Set lesson if provided
        if (request.getLessonId() != null) {
            Lesson lesson = lessonRepository.findById(request.getLessonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
            quiz.setLesson(lesson);
        }

        quiz.setModifiedBy(getCurrentUserId(instructorEmail));
        Quiz savedQuiz = quizRepository.save(quiz);

        return QuizMapper.toDto(savedQuiz);
    }

    @Transactional
    public QuizDto updateQuiz(UUID quizId, QuizRequest request) {
        String instructorEmail = getCurrentUserEmail();
        
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        verifyInstructorAccess(quiz.getCourse().getId(), instructorEmail);

        QuizMapper.updateEntityFromRequest(request, quiz);

        // Update lesson if provided
        if (request.getLessonId() != null) {
            Lesson lesson = lessonRepository.findById(request.getLessonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
            quiz.setLesson(lesson);
        } else {
            quiz.setLesson(null);
        }

        quiz.setModifiedBy(getCurrentUserId(instructorEmail));

        // Recalculate total marks
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByCreation(quizId);
        int totalMarks = questions.stream().mapToInt(QuizQuestion::getMarks).sum();
        quiz.setTotalMarks(totalMarks);

        Quiz updatedQuiz = quizRepository.save(quiz);
        return QuizMapper.toDto(updatedQuiz);
    }

    @Transactional
    public void deleteQuiz(UUID quizId) {
        String instructorEmail = getCurrentUserEmail();
        
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        verifyInstructorAccess(quiz.getCourse().getId(), instructorEmail);

        // Delete related submissions and questions (handled by cascade if configured)
        quizRepository.delete(quiz);
    }

    @Transactional
    public QuizDto addQuestionsToQuiz(UUID quizId, List<QuizQuestionRequest> requests) {
        String instructorEmail = getCurrentUserEmail();
        
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        verifyInstructorAccess(quiz.getCourse().getId(), instructorEmail);

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
        quiz.setModifiedBy(getCurrentUserId(instructorEmail));
        quizRepository.save(quiz);

        return QuizMapper.toDto(quiz);
    }

    @Transactional
    public void updateQuestion(UUID questionId, QuizQuestionRequest request) {
        String instructorEmail = getCurrentUserEmail();
        
        QuizQuestion question = quizQuestionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        verifyInstructorAccess(question.getQuiz().getCourse().getId(), instructorEmail);

        QuizQuestionMapper.updateEntityFromRequest(request, question);
        quizQuestionRepository.save(question);

        // Update total marks in quiz
        Quiz quiz = question.getQuiz();
        List<QuizQuestion> allQuestions = quizQuestionRepository.findByQuizIdOrderByCreation(quiz.getId());
        int totalMarks = allQuestions.stream().mapToInt(QuizQuestion::getMarks).sum();
        quiz.setTotalMarks(totalMarks);
        quiz.setModifiedBy(getCurrentUserId(instructorEmail));
        quizRepository.save(quiz);
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuizForStudent(UUID quizId, String userEmail) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        // Check if user is enrolled in the course
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get questions (without correct answers for students)
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByCreation(quizId);

        // Get user's attempt count
        int userAttempts = quizSubmissionRepository.countByQuizIdAndMemberId(quizId, user.getId());

        // Check if user can still attempt
        if (quiz.getMaxAttempts() > 0 && userAttempts >= quiz.getMaxAttempts()) {
            throw new InvalidRequestDataException("Maximum attempts reached for this quiz");
        }

        return QuizMapper.toResponse(quiz, questions, userAttempts);
    }

    @Transactional
    public QuizSubmissionResponse submitQuiz(UUID quizId, QuizSubmissionRequest request, String userEmail) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check attempt limits
        int userAttempts = quizSubmissionRepository.countByQuizIdAndMemberId(quizId, user.getId());
        if (quiz.getMaxAttempts() > 0 && userAttempts >= quiz.getMaxAttempts()) {
            throw new InvalidRequestDataException("Maximum attempts reached for this quiz");
        }

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
        submission.setCourse(quiz.getCourse());
        submission.setScore(score);
        submission.setScoreOutOf(quiz.getTotalMarks());
        submission.setPercentage(percentage);
        submission.setPassingPercentage(quiz.getPassingPercentage());

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

        // Allow access to instructor or the student who submitted
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isOwner = submission.getMember().getId().equals(user.getId());
        boolean isInstructor = isInstructorOfCourse(submission.getCourse().getId(), userEmail);

        if (!isOwner && !isInstructor) {
            throw new ForbiddenException("Access denied to this submission");
        }

        return QuizSubmissionMapper.toResponse(submission);
    }

    @Transactional(readOnly = true)
    public List<QuizSubmissionResponse> getCourseQuizSubmissions(UUID courseId) {
        String instructorEmail = getCurrentUserEmail();
        
        verifyInstructorAccess(courseId, instructorEmail);

        List<Quiz> quizzes = quizRepository.findByCourseId(courseId);
        return quizzes.stream()
                .flatMap(quiz -> quizSubmissionRepository.findByQuizIdOrderByCreationDesc(quiz.getId()).stream())
                .map(QuizSubmissionMapper::toResponse)
                .collect(Collectors.toList());
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

            if (userAnswer != null && userAnswer.trim().equalsIgnoreCase(correctAnswer.trim())) {
                isCorrect = true;
                marksAwarded = question.getMarks();
                totalScore += marksAwarded;
            } else if (quiz.getEnableNegativeMarking() && userAnswer != null && !userAnswer.trim().isEmpty()) {
                // Apply negative marking for wrong answers
                marksAwarded = -quiz.getMarksToCut();
                totalScore += marksAwarded; // This will subtract marks
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
        result.put("passed", percentage >= quiz.getPassingPercentage());

        return result;
    }

    private void verifyInstructorAccess(UUID courseId, String instructorEmail) {
        if (!isInstructorOfCourse(courseId, instructorEmail)) {
            throw new ForbiddenException("Access denied. You are not an instructor of this course");
        }
    }

    private boolean isInstructorOfCourse(UUID courseId, String email) {
        return courseInstructorRepository.existsByCourseIdAndInstructorEmail(courseId, email);
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    
    private UUID getCurrentUserId(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
