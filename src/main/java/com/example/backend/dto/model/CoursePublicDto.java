package com.example.backend.dto.model;

import com.example.backend.constant.CourseStatus;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class CoursePublicDto {
    private UUID id;
    private String title;
    private String slug;
    private String shortIntroduction;
    private String image;
    private String videoLink;
    private CourseStatus status;
    private Integer enrollments;
    private Integer lessons;
    private Double rating;
    private String language;
    private List<TagDto> tags;
    private List<LabelDto> labels;
}


