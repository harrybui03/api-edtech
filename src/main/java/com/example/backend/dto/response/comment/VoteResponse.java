package com.example.backend.dto.response.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteResponse {
    private UUID id;
    private UUID commentId;
    private String commentContent;
    private UUID lessonId;
    private String lessonTitle;
    private UUID userId;
    private String userName;
    private Boolean isUpvote; // true = UPVOTE, false = DOWNVOTE
    private OffsetDateTime creation;
}
