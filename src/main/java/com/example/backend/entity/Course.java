package com.example.backend.entity;

import com.example.backend.constant.CourseStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "courses")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true)
    private String slug;

    @Column(name = "short_introduction", columnDefinition = "TEXT")
    private String shortIntroduction;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String image;

    @Column(name = "video_link", length = 500)
    private String videoLink;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "course_status_enum default 'DRAFT'")
    private CourseStatus status;

    @Column
    private Boolean published;

    @Column(name = "published_on")
    private LocalDate publishedOn;

    @Column(name = "paid_course")
    private Boolean paidCourse;

    @Column(name = "course_price")
    private BigDecimal coursePrice;

    @Column(name = "selling_price")
    private BigDecimal sellingPrice;

    @Column
    private String currency;

    @Column(name = "amount_usd")
    private BigDecimal amountUsd;

    @Column(name = "enable_certification")
    private Boolean enableCertification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator")
    private User evaluator;

    @Column
    private Integer enrollments;

    @Column
    private Integer lessons;

    @Column
    private BigDecimal rating;

    @Column
    private String language;

    @CreationTimestamp
    @Column(name = "creation", nullable = false, updatable = false)
    private OffsetDateTime creation;

    @UpdateTimestamp
    @Column(name = "modified", nullable = false)
    private OffsetDateTime modified;

    @Column(name = "modified_by")
    private UUID modifiedBy;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Chapter> chapters;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Lesson> lessonsList;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CourseInstructor> instructors;
}
