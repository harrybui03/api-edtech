package com.example.backend.dto.request.instructor;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class InstructorIdsRequest {
    private List<UUID> instructorIds;
}