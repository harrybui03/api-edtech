package com.example.backend.excecption.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields
@Builder // Generates a builder pattern for object creation
public class ApiErrorDetail {
    private String issue; // A specific issue or problem description
    private String field; // Optional: The field name related to the issue (e.g., for validation errors)
    private String value; // Optional: The value of the field that caused the issue
    private String code; // Optional: A specific error code for the issue
}
