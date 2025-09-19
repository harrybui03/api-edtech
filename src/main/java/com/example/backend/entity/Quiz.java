package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "quizzes")
@Getter
@Setter
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "max_attempts")
    private Integer maxAttempts = 0;

    @Column(name = "show_answers")
    private Boolean showAnswers = true;

    @Column(name = "show_submission_history")
    private Boolean showSubmissionHistory = false;

    @Column(name = "total_marks")
    private Integer totalMarks = 0;

    @Column(name = "passing_percentage", nullable = false)
    private Integer passingPercentage;

    private String duration;

    @Column(name = "shuffle_questions")
    private Boolean shuffleQuestions = false;

    @Column(name = "limit_questions_to")
    private Integer limitQuestionsTo;

    @Column(name = "enable_negative_marking")
    private Boolean enableNegativeMarking = false;

    @Column(name = "marks_to_cut")
    private Integer marksToCut = 1;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime creation;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime modified;

    @Column(name = "modified_by")
    private UUID modifiedBy;
}

