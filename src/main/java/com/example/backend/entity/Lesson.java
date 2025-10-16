package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "lessons")
@Getter
@Setter
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "position")
    private Integer position;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id" , referencedColumnName = "id")
    private Quiz quiz;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime creation;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime modified;

    @Column(name = "modified_by")
    private UUID modifiedBy;
}
