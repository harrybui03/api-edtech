package com.example.backend.excecption.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Represents a standardized API error response.
 * This class uses Lombok annotations for automatic generation of
 * getters, setters, constructors, and a builder pattern.
 */
@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields
@Builder // Generates a builder pattern for object creation
public class ApiError {
    @Builder.Default
    private String errorId = UUID.randomUUID().toString();
    private String message;
    private String issue;
    private HttpStatus httpStatus;
    @Builder.Default
    private Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
}
