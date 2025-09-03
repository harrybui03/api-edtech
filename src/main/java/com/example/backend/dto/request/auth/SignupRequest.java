package com.example.backend.dto.request.auth;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SignupRequest {
    private String fullName;
    private String email;
}
