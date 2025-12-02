package com.example.backend.dto.response.statistics;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RevenueOverTimeResponse {
    private String type;
    private String period;
    private String groupBy;
    private String currency;
    private List<RevenueDataPoint> dataPoints;
}