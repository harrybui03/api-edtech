package com.example.backend.dto.response.comment;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CommentResponse {
    private UUID id;
    private UUID lessonId;
    private String lessonTitle;
    private UUID authorId;
    private String authorName;
    private String authorImage;
    private String content;
    private UUID parentId;
    private List<CommentResponse> replies;
    private Integer upvotes;
    private Integer downvotes;
    private Boolean canEdit;
    private Boolean canDelete;
    private String userVote; // "upvote", "downvote", or null
    private OffsetDateTime creation;
    private OffsetDateTime modified;
}
