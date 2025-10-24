package com.example.backend.dto.request.payos;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdatePayOSConfigRequest {
    private String clientId;
    private String apiKey;
    private String checksumKey;
    private String accountNumber;
    private Boolean isActive; // Allow enabling/disabling the config
}