package com.example.backend.dto.request.course;

import com.example.backend.dto.model.LabelDto;
import com.example.backend.dto.model.TagDto;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CourseRequest {
    private String title;
    private String shortIntroduction;
    private String description;
    private String image;
    private String videoLink;
    private Boolean paidCourse;
    private BigDecimal coursePrice;
    private BigDecimal sellingPrice;
    private String currency;
    private BigDecimal amountUsd;
    private Boolean enableCertification;
    private String language;
    private List<TagDto> tagDto;
    private List<LabelDto> labelDto;
}
