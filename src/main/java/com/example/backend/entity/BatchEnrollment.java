package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "batch_enrollments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "batch_id"})
})
@Getter
@Setter
public class BatchEnrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    private String source;

    @Column(name = "confirmation_email_sent")
    private Boolean confirmationEmailSent = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime creation;
}

