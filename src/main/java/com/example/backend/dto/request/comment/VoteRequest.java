package com.example.backend.dto.request.comment;

import com.example.backend.constant.VoteType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoteRequest {
    @NotNull(message = "Vote type is required")
    private VoteType voteType;
}
