package com.example.backend.dto.request.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBatchDocumentRequest {
    @NotNull(message = "Batch Discussion ID cannot be null")
    private UUID batchDiscussionId;

    @NotBlank(message = "File URL cannot be blank")
    private String fileUrl;
}