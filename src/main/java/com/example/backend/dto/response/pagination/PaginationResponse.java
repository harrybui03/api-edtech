package com.example.backend.dto.response.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginationResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private int totalElements;
    private int totalPages;
}

