package com.example.backend.dto.model;

import lombok.Data;
import java.util.UUID;

@Data
public class InstructorDto {
    private UUID id;
    private String fullName;
    private String email;
    private String userImage;
    
    public InstructorDto(UUID id, String fullName, String email, String userImage) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.userImage = userImage;
    }
}
