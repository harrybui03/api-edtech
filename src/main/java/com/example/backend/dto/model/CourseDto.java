package com.example.backend.dto.model;

import com.example.backend.constant.CourseStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CourseDto {
    private UUID id;
    private String title;
    private String shortIntroduction;
    private String description;
    private String image;
    private String category;
    private CourseStatus status;
    private Boolean published;
    private LocalDate publishedOn;
    private BigDecimal coursePrice;
    private Integer enrollments;
    private Integer lessons;
    private BigDecimal rating;
}
