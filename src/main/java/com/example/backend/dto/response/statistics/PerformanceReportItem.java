package com.example.backend.dto.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReportItem {
    private UUID id;
    private String title;
    private BigDecimal totalRevenue;
    private Long enrollmentCount;
}