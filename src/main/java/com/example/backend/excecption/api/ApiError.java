package com.example.backend.excecption.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

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
    private String errorId; // Unique identifier for this specific error instance
    private String message; // A general message describing the error
    private HttpStatus httpStatus; // The HTTP status code associated with the error
    private LocalDateTime timestamp; // The time when the error occurred

    /**
     * Custom builder method to set a default errorId and timestamp if not provided.
     * This ensures every ApiError has a unique ID and a timestamp.
     * @return A new ApiErrorBuilder instance.
     */
    public static ApiErrorBuilder builder() {
        return new CustomApiErrorBuilder()
                .errorId(UUID.randomUUID().toString()) // Assign a unique ID
                .timestamp(LocalDateTime.now()); // Set the current timestamp
    }

    // Custom builder class to override the default builder behavior
    private static class CustomApiErrorBuilder extends ApiErrorBuilder {
        // No additional methods needed here, just inheriting and allowing the
        // static builder() method in ApiError to return this custom builder.
    }
}
