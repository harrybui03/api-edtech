package com.example.backend.excecption.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum Status {
    // 4xx
    BAD_REQUEST("BAD_REQUEST", "Bad Request", "Bad Request", HttpStatus.BAD_REQUEST),
    NOT_FOUND("NOT_FOUND", "Not Found", "Not Found", HttpStatus.NOT_FOUND),

    // 5xx
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Internal Server Error", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String errorId;
    private final String message;
    private final String issue;
    private final HttpStatus status;
}
