package com.example.backend.dto.response.payment;

import com.example.backend.dto.response.pagination.PaginationMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionListResponse {

    private List<TransactionSummaryResponse> content;
    private PaginationMetadata pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionSummaryResponse {
        private String id;
        private String orderCode;
        private String studentName;
        private String instructorName;
        private String courseTitle;
        private String amount;
        private String currency;
        private String status;
        private String createdAt;
        private String paidAt;
    }
}
