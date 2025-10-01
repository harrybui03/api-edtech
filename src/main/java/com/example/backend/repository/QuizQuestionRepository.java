package com.example.backend.repository;

import com.example.backend.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, UUID> {
    
    List<QuizQuestion> findByQuizIdOrderByCreation(UUID quizId);
    
    void deleteByQuizId(UUID quizId);
}
