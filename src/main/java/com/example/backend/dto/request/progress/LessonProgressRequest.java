package com.example.backend.dto.request.progress;

import lombok.Data;

@Data
public class LessonProgressRequest {
    // No additional fields needed - lesson ID comes from path parameter
    // Status will be automatically set to COMPLETE
}
