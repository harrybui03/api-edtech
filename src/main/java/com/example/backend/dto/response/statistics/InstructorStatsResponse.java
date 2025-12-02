package com.example.backend.dto.response.statistics;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class InstructorStatsResponse {
    private long totalCoursePublished;
    private long totalBatchPublished;
    private BigDecimal courseRevenue;
    private  BigDecimal batchRevenue;
}
