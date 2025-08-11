package com.example.backend.entity;

import com.example.backend.constant.EnrollmentMemberType;
import com.example.backend.constant.EnrollmentRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "course_id"})
})
@Getter
@Setter
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", columnDefinition = "enrollment_member_type_enum")
    private EnrollmentMemberType memberType;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", columnDefinition = "enrollment_role_enum")
    private EnrollmentRole role;

    private BigDecimal progress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_lesson")
    private Lesson currentLesson;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "purchased_certificate")
    private Boolean purchasedCertificate;

    // The certificate_id is mapped with a direct relationship
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_id")
    private Certificate certificate;

    @Column(name = "cohort_id")
    private UUID cohortId;

    @Column(name = "subgroup_id")
    private UUID subgroupId;

    @Column(name = "batch_old_id")
    private UUID batchOldId;
    // ... other UUID fields like cohort_id etc.

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime creation;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime modified;
}
