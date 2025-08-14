package com.example.backend.entity;

import com.example.backend.constant.BatchMedium;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "batches")
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

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    private String timezone;
    private Boolean published = false;

    @Column(name = "allow_self_enrollment")
    private Boolean allowSelfEnrollment = false;

    private Boolean certification = false;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "batch_medium_enum default 'ONLINE'")
    private BatchMedium medium;

    private String category;

    @Column(name = "seat_count")
    private Integer seatCount;

    @Column(name = "evaluation_end_date")
    private LocalDate evaluationEndDate;

    @Column(name = "meta_image")
    private String metaImage;

    @Column(name = "batch_details", columnDefinition = "TEXT")
    private String batchDetails;

    @Column(name = "batch_details_raw", columnDefinition = "TEXT")
    private String batchDetailsRaw;

    @Column(name = "show_live_class")
    private Boolean showLiveClass = false;

    @Column(name = "allow_future")
    private Boolean allowFuture = false;

    @Column(name = "paid_batch")
    private Boolean paidBatch = false;

    private BigDecimal amount;
    private String currency;

    @Column(name = "amount_usd")
    private BigDecimal amountUsd;

    @Column(name = "custom_component", columnDefinition = "TEXT")
    private String customComponent;

    @Column(name = "custom_script", columnDefinition = "TEXT")
    private String customScript;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime creation;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime modified;

    @Column(name = "modified_by")
    private UUID modifiedBy;
}