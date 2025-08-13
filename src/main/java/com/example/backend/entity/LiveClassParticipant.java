package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "live_class_participants")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LiveClassParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "live_class_id", nullable = false)
    private LiveClass liveClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt;

    @Column(name = "left_at", nullable = false)
    private OffsetDateTime leftAt;

    @Column(nullable = false)
    private Integer duration;

    @CreationTimestamp
    @Column(name = "creation", nullable = false, updatable = false)
    private OffsetDateTime creation;
} 