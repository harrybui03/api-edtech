package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payos_configs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayOSConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "api_key", nullable = false)
    private String apiKey;

    @Column(name = "checksum_key", nullable = false)
    private String checksumKey;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
