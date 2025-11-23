package com.example.backend.dto.response.live;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkUploadResponse {
    
    private UUID chunkId;
    private Integer chunkIndex;
    private String message;
    private Long fileSize;
    private Integer totalChunksUploaded;
}

