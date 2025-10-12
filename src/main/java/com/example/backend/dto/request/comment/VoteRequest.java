package com.example.backend.dto.request.comment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoteRequest {
    @NotNull(message = "Vote type is required")
    private Boolean isUpvote; // true = UPVOTE, false = DOWNVOTE
}
