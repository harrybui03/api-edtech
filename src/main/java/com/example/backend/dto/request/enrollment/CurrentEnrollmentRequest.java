package com.example.backend.dto.request.enrollment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class CurrentEnrollmentRequest {
    FilterBy filterBy;
    int page;
    int size;
    public enum FilterBy {
        COURSE, BATCH
    }
}
