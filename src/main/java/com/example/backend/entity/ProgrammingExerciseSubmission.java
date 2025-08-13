package com.example.backend.entity;

import com.example.backend.constant.ProgrammingExerciseStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "programming_exercise_submissions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProgrammingExerciseSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private ProgrammingExercise exercise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "programming_exercise_status_enum")
    private ProgrammingExerciseStatus status;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String code;

    @Column(name = "test_cases", columnDefinition = "JSONB")
    private String testCases;

    @CreationTimestamp
    @Column(name = "creation", nullable = false, updatable = false)
    private OffsetDateTime creation;

    @UpdateTimestamp
    @Column(name = "modified", nullable = false)
    private OffsetDateTime modified;
} 