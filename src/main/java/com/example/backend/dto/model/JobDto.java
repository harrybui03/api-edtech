package com.example.backend.dto.model;

import com.example.backend.constant.UploadPurpose;
import com.example.backend.constant.JobStatus;
import com.example.backend.constant.JobType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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