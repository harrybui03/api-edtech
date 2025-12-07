package com.example.backend.dto.response.payment;

import com.example.backend.constant.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private UUID id;
    private String orderCode;
    private UUID studentId;
    private String studentName;
    private UUID instructorId;
    private String instructorName;
    private UUID courseId;
    private String courseTitle;
    private UUID batchId;
    private String batchTitle;
    private String paymentId;
    private String paymentUrl;
    private String qrCode;
    private String accountNumber;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    private String description;
    private String returnUrl;
    private String cancelUrl;
    private OffsetDateTime paidAt;
    private OffsetDateTime failedAt;
    private Boolean webhookReceived;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
