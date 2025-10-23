package com.example.backend.dto.model;

import com.example.backend.constant.EntityType;
import com.example.backend.constant.UploadPurpose;
import com.example.backend.constant.job.JobStatus;
import com.example.backend.constant.job.JobType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobDto {
    private UUID id;
    private JobType jobType;
    private JobStatus status;
    private UUID entityId;
    private UploadPurpose entityType;
    private OffsetDateTime createdAt;
}