package com.example.backend.dto.request.live;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartLiveRequest {
    
    @NotNull(message = "Batch ID is required")
    private UUID batchId;
    
    private String title;
    
    private String description;
}

