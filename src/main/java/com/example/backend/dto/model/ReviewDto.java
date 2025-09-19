package com.example.backend.dto.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ReviewDto {
    private UUID id;
    private UUID courseId;
    private UUID studentId;
    private String studentName;
    private String studentImage;
    private Integer rating;
    private String comment;
    private Boolean isApproved;
    private UUID approvedBy;
    private OffsetDateTime approvedAt;
    private OffsetDateTime creation;
    private OffsetDateTime modified;
}
