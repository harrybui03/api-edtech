package com.example.backend.dto.model;

import com.example.backend.constant.CourseStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CoursePublicDto {
    private UUID id;
    private String title;
    private String slug;
    private String shortIntroduction;
    private String description;
    private String image;
    private CourseStatus status;
    private BigDecimal sellingPrice;
    private String currency;
    private Integer enrollments;
    private Integer lessons;
    private Double rating;
    private String videoLink;
    private String language;
    private String targetAudience;
    private String skillLevel;
    private String learnerProfileDesc;
    private Boolean paidCourse;
    private List<TagDto> tags;
    private List<LabelDto> labels;
    private List<InstructorDto> instructors;
}


