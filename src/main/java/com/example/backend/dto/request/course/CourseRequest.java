package com.example.backend.dto.request.course;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseRequest {
    private String title;
    private String shortIntroduction;
    private String description;
    private String category;
    private BigDecimal coursePrice;
}