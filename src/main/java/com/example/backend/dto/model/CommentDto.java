package com.example.backend.dto.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CommentDto {
    private UUID id;
    private UUID lessonId;
    private UUID authorId;
    private String authorName;
    private String authorImage;
    private String content;
    private UUID parentId;
    private List<CommentDto> replies;
    private Integer upvotes;
    private Integer downvotes;
    private Boolean isDeleted;
    private Boolean canEdit; // Helper field to determine if current user can edit
    private Boolean canDelete; // Helper field to determine if current user can delete
    private String userVote; // Current user's vote: "upvote", "downvote", or null
    private OffsetDateTime creation;
    private OffsetDateTime modified;
}
