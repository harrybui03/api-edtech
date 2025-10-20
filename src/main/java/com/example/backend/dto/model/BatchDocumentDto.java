package com.example.backend.dto.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchDocumentDto {
    private UUID id;
    private UUID batchDiscussionId;
    private String fileUrl;
    private LocalDateTime uploadedAt;
}
