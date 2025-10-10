package com.example.backend.dto.model;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class ChapterPublicDto {
    private UUID id;
    private String title;
    private String slug;
    private Integer position;
    private List<LessonPublicDto> lessons;
}


