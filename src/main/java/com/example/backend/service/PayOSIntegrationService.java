package com.example.backend.service;

import com.example.backend.entity.PayOSConfig;
import com.example.backend.entity.Transaction;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSIntegrationService {

    // Using PayOS SDK instead of RestTemplate
    private final ObjectMapper objectMapper;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public PayOSPaymentResponse createPaymentRequest(PayOSConfig config, Transaction transaction) {
        log.info("Creating PayOS payment request for transaction: {}", transaction.getOrderCode());

        try {
            // Use PayOS Java SDK
            PayOS payOS = new PayOS(
                    config.getClientId(),
                    config.getApiKey(),
                    config.getChecksumKey()
            );

            String itemName = transaction.getCourse() != null
                    ? transaction.getCourse().getTitle()
                    : (transaction.getBatch() != null ? transaction.getBatch().getTitle() : "Item");
            ItemData item = ItemData.builder()
                    .name(itemName)
                    .quantity(1)
                    .price(transaction.getAmount().intValue())
                    .build();

            String description = transaction.getDescription();
            String shortDescription = description != null && description.length() > 25
                    ? description.substring(0, 25)
                    : description;

            PaymentData paymentData = PaymentData.builder()
                    .orderCode(transaction.getOrderCode())
                    .amount(transaction.getAmount().intValue())
                    .description(shortDescription)
                    .returnUrl(transaction.getReturnUrl())
                    .cancelUrl(transaction.getCancelUrl())
                    .item(item) // or .items(List.of(item)) depending on SDK version
                    .build();

            CheckoutResponseData sdkRes = payOS.createPaymentLink(paymentData);

            PayOSPaymentResponse res = new PayOSPaymentResponse();
            res.setCode("00");
            res.setMessage("success");
            PayOSPaymentResponse.Data data = new PayOSPaymentResponse.Data();
            data.setPaymentId(sdkRes.getPaymentLinkId());
            data.setPaymentUrl(sdkRes.getCheckoutUrl());
            data.setQrCode(sdkRes.getQrCode());
            res.setData(data);
            return res;

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

    // Verify webhook using SDK and return code ("00" success)
    public String verifyWebhookAndGetCode(String rawBody, PayOSConfig config) {
        try {
            PayOS payOS = new PayOS(config.getClientId(), config.getApiKey(), config.getChecksumKey());

            // Normalize payload to satisfy SDK schema (fill missing required fields)
            com.fasterxml.jackson.databind.node.ObjectNode root = (com.fasterxml.jackson.databind.node.ObjectNode) objectMapper.readTree(rawBody);
            com.fasterxml.jackson.databind.node.ObjectNode data = (com.fasterxml.jackson.databind.node.ObjectNode) root.with("data");

            if (!data.hasNonNull("reference")) data.put("reference", "TF000000000000");
            if (!data.has("accountNumber")) data.put("accountNumber", "");
            if (!data.has("currency")) data.put("currency", "VND");
            if (!data.has("paymentLinkId")) data.put("paymentLinkId", "");
            if (!data.has("transactionDateTime")) data.put("transactionDateTime", "1970-01-01 00:00:00");
            if (!data.has("code")) data.put("code", "00");
            if (!data.has("desc")) data.put("desc", "success");

            Webhook webhook = objectMapper.treeToValue(root, Webhook.class);
            WebhookData webhookData = payOS.verifyPaymentWebhookData(webhook);
            return webhookData.getCode();
        } catch (Exception e) {
            log.error("SDK webhook verify failed: {}", e.getMessage(), e);
            throw new RuntimeException("SDK webhook verify failed: " + e.getMessage());
        }
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
            private String qrCode;

            public String getPaymentId() { return paymentId; }
            public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

            public String getPaymentUrl() { return paymentUrl; }
            public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }
            public String getQrCode() { return qrCode; }
            public void setQrCode(String qrCode) { this.qrCode = qrCode; }
        }
    }
}
