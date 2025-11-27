package com.example.backend.entity;

import com.example.backend.constant.BatchStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "batch")
@Getter
@Setter
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(unique = true, nullable = false)
    private String slug;

    private String image;

    @Column(name = "video_link")
    private String videoLink;

    @Column(name = "paid_batch", nullable = false)
    private boolean paidBatch;

    @Column(name = "actual_price")
    private BigDecimal actualPrice;

    @Column(name = "selling_price")
    private BigDecimal sellingPrice;

    @Column(name = "amount_usd")
    private BigDecimal amountUsd;

    private String language;

    @Column(name = "open_time")
    private LocalDateTime openTime;

    @Column(name = "close_time")
    private LocalDateTime closeTime;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BatchStatus status;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BatchInstructor> instructors;
}
