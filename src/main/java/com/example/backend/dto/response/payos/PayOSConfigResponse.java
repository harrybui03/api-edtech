package com.example.backend.dto.response.payos;

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
public class PayOSConfigResponse {

    private UUID id;
    private UUID instructorId;
    private String instructorName;
    private String clientId;
    private String apiKey;
    private String checksumKey;
    private String accountNumber;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
