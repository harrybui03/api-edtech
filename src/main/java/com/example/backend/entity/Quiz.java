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

    @Column(name = "show_answers")
    private Boolean showAnswers = true;

    @Column(name = "show_submission_history")
    private Boolean showSubmissionHistory = false;

    @Column(name = "total_marks")
    private Integer totalMarks = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime creation;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime modified;

    @Column(name = "modified_by")
    private UUID modifiedBy;
}
