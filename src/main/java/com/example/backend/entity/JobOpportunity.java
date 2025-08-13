package com.example.backend.entity;

import com.example.backend.constant.JobStatus;
import com.example.backend.constant.JobType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_opportunities")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobOpportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "job_type_enum default 'FULL_TIME'")
    private JobType type;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "job_status_enum default 'OPEN'")
    private JobStatus status;

    @Column(columnDefinition = "boolean default false")
    private Boolean disabled;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_website", nullable = false)
    private String companyWebsite;

    @Column(name = "company_logo", nullable = false)
    private String companyLogo;

    @Column(name = "company_email_address", nullable = false)
    private String companyEmailAddress;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @CreationTimestamp
    @Column(name = "creation", nullable = false, updatable = false)
    private OffsetDateTime creation;

    @UpdateTimestamp
    @Column(name = "modified", nullable = false)
    private OffsetDateTime modified;

    @Column(name = "modified_by")
    private UUID modifiedBy;
} 