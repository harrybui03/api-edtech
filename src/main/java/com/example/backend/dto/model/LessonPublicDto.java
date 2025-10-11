package com.example.backend.dto.model;

import lombok.Data;
import java.util.UUID;

@Data
public class LessonPublicDto {
    private UUID id;
    private String title;
    private String slug;
    private Integer position;
}


