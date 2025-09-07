package com.example.backend.dto.request.instructor;

import lombok.Data;
import java.util.UUID;

@Data
public class InstructorIdRequest {
    private UUID instructorId;
}