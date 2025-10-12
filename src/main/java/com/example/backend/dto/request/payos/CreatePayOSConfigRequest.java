package com.example.backend.dto.request.payos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePayOSConfigRequest {

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotBlank(message = "API Key is required")
    private String apiKey;

    @NotBlank(message = "Checksum Key is required")
    private String checksumKey;

    @NotBlank(message = "Account Number is required")
    private String accountNumber;
}
