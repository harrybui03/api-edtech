package com.example.backend.dto.response.pagination;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PaginationResponse<T> {
    private final List<T> content;
    private final PaginationMetadata pagination;

    public PaginationResponse(Page<T> page) {
        this.content = page.getContent();
        this.pagination = new PaginationMetadata(page);
    }
}