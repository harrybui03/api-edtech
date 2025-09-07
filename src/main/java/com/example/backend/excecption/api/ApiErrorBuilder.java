package com.example.backend.excecption.api;

import org.springframework.http.ResponseEntity;

public class ApiErrorBuilder {
    public static ResponseEntity<ApiError> buildApiErrorResponse(Exception e, Status status) {
        return new ResponseEntity<>(
                ApiError.builder()
                        .message(e.getMessage())
                        .issue(status.getIssue())
                        .httpStatus(status.getStatus())
                        .build(),
                status.getStatus());
    }

    public static ResponseEntity<ApiError> buildApiErrorResponse(Exception e, Status status, String customIssue) {
        return new ResponseEntity<>(
                ApiError.builder()
                        .message(e.getMessage())
                        .issue(customIssue)
                        .httpStatus(status.getStatus())
                        .build(),
                status.getStatus());
    }
}
