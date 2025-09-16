package com.example.backend.repository;

import com.example.backend.entity.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, UUID> {
    
    List<QuizSubmission> findByQuizIdAndMemberIdOrderByCreationDesc(UUID quizId, UUID memberId);
    
    List<QuizSubmission> findByQuizIdOrderByCreationDesc(UUID quizId);
    
    List<QuizSubmission> findByMemberIdOrderByCreationDesc(UUID memberId);
    
    int countByQuizIdAndMemberId(UUID quizId, UUID memberId);
}