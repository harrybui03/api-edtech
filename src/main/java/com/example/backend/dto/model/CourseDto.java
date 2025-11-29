package com.example.backend.dto.model;

import com.example.backend.constant.CourseStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CourseDto {
    private UUID id;
    private String title;
    private String slug;
    private String shortIntroduction;
    private String description;
    private String image;
    private String videoLink;
    private CourseStatus status;
    private Boolean paidCourse;
    private BigDecimal coursePrice;
    private BigDecimal sellingPrice;
    private String currency;
    private BigDecimal amountUsd;
    private Integer enrollments;
    private Integer lessons;
    private BigDecimal rating;
    private String language;
    private String targetAudience;
    private String skillLevel;
    private String learnerProfileDesc;
    private List<TagDto> tags;
    private List<LabelDto> labels;
    private List<ChapterDto> chapters;
    private List<UserDTO> instructors;
}
