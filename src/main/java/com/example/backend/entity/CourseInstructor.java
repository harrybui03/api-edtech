package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "course_instructors", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"course_id", "instructor_id"})
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseInstructor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime creation;
}

