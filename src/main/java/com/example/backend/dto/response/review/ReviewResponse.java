package com.example.backend.dto.response.review;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ReviewResponse {
    private UUID id;
    private UUID courseId;
    private String courseName;
    private UUID studentId;
    private String studentName;
    private String studentImage;
    private Integer rating;
    private String comment;
    private Boolean isApproved;
    private OffsetDateTime creation;
    private OffsetDateTime modified;
}
