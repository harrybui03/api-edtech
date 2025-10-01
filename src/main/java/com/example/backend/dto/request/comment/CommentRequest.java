package com.example.backend.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CommentRequest {
    
    @NotBlank(message = "Comment content is required")
    private String content;
    
    // Optional: If provided, this comment will be a reply to the specified parent comment
    private UUID parentId;
}
