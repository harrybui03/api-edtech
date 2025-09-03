package com.example.backend.excecption.api;

import org.springframework.http.ResponseEntity;

public class ApiErrorBuilder {
    public static ResponseEntity<ApiError> buildApiErrorResponse(Exception e , Status status) {
        return new ResponseEntity<>(
                ApiError.builder()
                        .message(e.getMessage())
                        .httpStatus(status.getStatus())
                        .build(),
                status.getStatus());
    }
}
