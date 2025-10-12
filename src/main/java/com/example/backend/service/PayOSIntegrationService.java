package com.example.backend.service;

import com.example.backend.entity.PayOSConfig;
import com.example.backend.entity.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSIntegrationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public PayOSPaymentResponse createPaymentRequest(PayOSConfig config, Transaction transaction) {
        log.info("Creating PayOS payment request for transaction: {}", transaction.getOrderCode());

        try {
            String url = "https://api-merchant.payos.vn/v2/payment-requests";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("orderCode", transaction.getOrderCode());
            requestBody.put("amount", transaction.getAmount().intValue());
            requestBody.put("description", transaction.getDescription());
            requestBody.put("items", createItems(transaction));
            requestBody.put("returnUrl", transaction.getReturnUrl());
            requestBody.put("cancelUrl", transaction.getCancelUrl());
            requestBody.put("accountNumber", config.getAccountNumber());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-client-id", config.getClientId());
            headers.set("x-api-key", config.getApiKey());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<PayOSPaymentResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, PayOSPaymentResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("PayOS payment request created successfully for order: {}", transaction.getOrderCode());
                return response.getBody();
            } else {
                log.error("Failed to create PayOS payment request. Status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to create PayOS payment request");
            }

        } catch (Exception e) {
            log.error("Error creating PayOS payment request: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating PayOS payment request: " + e.getMessage());
        }
    }

    public boolean verifyWebhookSignature(String signature, String body, String checksumKey) {
        try {
            String expectedSignature = generateHmacSha256(body, checksumKey);
            return signature.equals(expectedSignature);
        } catch (Exception e) {
            log.error("Error verifying webhook signature: {}", e.getMessage(), e);
            return false;
        }
    }

    private Map<String, Object> createItems(Transaction transaction) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", transaction.getCourse().getTitle());
        item.put("quantity", 1);
        item.put("price", transaction.getAmount().intValue());

        return Map.of("items", new Object[]{item});
    }

    private String generateHmacSha256(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    public static class PayOSPaymentResponse {
        private String code;
        private String message;
        private Data data;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Data getData() { return data; }
        public void setData(Data data) { this.data = data; }

        public static class Data {
            private String paymentId;
            private String paymentUrl;

            public String getPaymentId() { return paymentId; }
            public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

            public String getPaymentUrl() { return paymentUrl; }
            public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }
        }
    }
}
