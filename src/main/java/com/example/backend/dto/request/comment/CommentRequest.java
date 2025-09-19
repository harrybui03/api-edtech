package com.example.backend.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequest {
    
    @NotBlank(message = "Comment content is required")
    private String content;
}
