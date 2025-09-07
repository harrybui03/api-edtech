package com.example.backend.dto.response.pagination;

import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

@Getter
public class PaginationMetadata {
    private final int number;
    private final int totalPages;
    private final long totalElements;
    private final Sort sort;

    public PaginationMetadata(Page<?> page) {
        this.number = page.getNumber();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.sort = page.getSort();
    }
}