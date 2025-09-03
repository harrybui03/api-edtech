package com.example.backend.dto.request.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshTokenRequest {
    String refreshToken;
}
