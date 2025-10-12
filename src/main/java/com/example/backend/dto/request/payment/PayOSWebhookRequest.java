package com.example.backend.dto.request.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOSWebhookRequest {

    private String code;
    private String desc;
    private Data data;
    private String signature;

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private String orderCode;
        private String paymentId;
        private String accountNumber;
        private String amount;
        private String description;
        private String transactionDateTime;
        private String currency;
        private String status;
    }
}
