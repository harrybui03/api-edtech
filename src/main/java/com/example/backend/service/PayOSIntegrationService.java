package com.example.backend.service;

import com.example.backend.entity.PayOSConfig;
import com.example.backend.entity.Transaction;
import vn.payos.PayOS;
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

            String description = transaction.getDescription();
            String shortDescription = description != null && description.length() > 25
                    ? description.substring(0, 25)
                    : description;

            try {
                Class<?> createPaymentLinkRequestClass = Class.forName("vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest");
                Object paymentRequestBuilder = createPaymentLinkRequestClass.getMethod("builder").invoke(null);
                Class<?> builderClass = paymentRequestBuilder.getClass();
                
                // Set orderCode (Long)
                try {
                    builderClass.getMethod("orderCode", Long.class).invoke(paymentRequestBuilder, transaction.getOrderCode());
                } catch (NoSuchMethodException e) {
                    builderClass.getMethod("orderCode", long.class).invoke(paymentRequestBuilder, transaction.getOrderCode());
                }
                
                try {
                    builderClass.getMethod("amount", Long.class).invoke(paymentRequestBuilder, transaction.getAmount().longValue());
                } catch (NoSuchMethodException e) {
                    builderClass.getMethod("amount", long.class).invoke(paymentRequestBuilder, transaction.getAmount().longValue());
                }
                
                // Set description, returnUrl, cancelUrl
                builderClass.getMethod("description", String.class).invoke(paymentRequestBuilder, 
                    shortDescription != null ? shortDescription : "Thanh toán đơn hàng");
                builderClass.getMethod("returnUrl", String.class).invoke(paymentRequestBuilder, transaction.getReturnUrl());
                builderClass.getMethod("cancelUrl", String.class).invoke(paymentRequestBuilder, transaction.getCancelUrl());
                
                Object paymentRequest = builderClass.getMethod("build").invoke(paymentRequestBuilder);
                
                log.debug("Payment request object created successfully");
                
                // Use paymentRequests().create() method
                Object paymentRequests = payOS.getClass().getMethod("paymentRequests").invoke(payOS);
                log.debug("Calling paymentRequests().create()...");
                Object paymentLink = paymentRequests.getClass().getMethod("create", createPaymentLinkRequestClass).invoke(paymentRequests, paymentRequest);
                
                // Extract response data
                String paymentLinkId = null;
                String checkoutUrl = null;
                String qrCode = null;
                
                // Get checkoutUrl (required)
                checkoutUrl = (String) paymentLink.getClass().getMethod("getCheckoutUrl").invoke(paymentLink);
                
                // Try to get payment ID
                try {
                    Object id = paymentLink.getClass().getMethod("getId").invoke(paymentLink);
                    paymentLinkId = id != null ? id.toString() : null;
                } catch (NoSuchMethodException e) {
                    try {
                        paymentLinkId = (String) paymentLink.getClass().getMethod("getPaymentLinkId").invoke(paymentLink);
                    } catch (NoSuchMethodException ex) {
                        // Payment ID not available
                    }
                }
                
                // Try to get QR code
                try {
                    qrCode = (String) paymentLink.getClass().getMethod("getQrCode").invoke(paymentLink);
                } catch (NoSuchMethodException e) {
                    // QR code not available
                }
                
                PayOSPaymentResponse res = new PayOSPaymentResponse();
                res.setCode("00");
                res.setMessage("success");
                PayOSPaymentResponse.Data data = new PayOSPaymentResponse.Data();
                data.setPaymentId(paymentLinkId);
                data.setPaymentUrl(checkoutUrl);
                data.setQrCode(qrCode);
                res.setData(data);
                return res;
                
            } catch (Exception e) {
                log.error("Error using PayOS SDK 2.0.1 API: {}", e.getMessage(), e);
                
                // Check if it's a signature error
                if (e.getMessage() != null && e.getMessage().contains("signature") || 
                    (e.getCause() != null && e.getCause().getMessage() != null && 
                     e.getCause().getMessage().contains("signature"))) {
                    log.error("PayOS signature error detected. Please verify:");
                    log.error("1. Checksum key is correct in PayOSConfig");
                    log.error("2. Client ID, API Key, and Checksum Key match your PayOS account");
                    log.error("3. All required fields are provided (orderCode, amount, description, returnUrl, cancelUrl)");
                    throw new RuntimeException("PayOS signature validation failed. Please check your PayOS configuration (Client ID, API Key, Checksum Key) are correct and match your PayOS account.", e);
                }
                
                throw new RuntimeException("Error creating PayOS payment request: " + e.getMessage(), e);
            }

        } catch (RuntimeException e) {
            // Re-throw runtime exceptions as-is
            throw e;
        } catch (Exception e) {
            log.error("Error creating PayOS payment request: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating PayOS payment request: " + e.getMessage(), e);
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

    public String verifyWebhookAndGetCode(String rawBody, PayOSConfig config) {
        try {
            // Parse webhook JSON
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(rawBody);
            
            // Extract signature from webhook
            String signature = rootNode.has("signature") ? rootNode.get("signature").asText() : null;
            
            if (signature == null) {
                log.warn("Webhook missing signature");
                return "01"; // Invalid - no signature
            }
            
            // Remove signature from body for verification
            com.fasterxml.jackson.databind.node.ObjectNode bodyWithoutSignature = rootNode.deepCopy();
            bodyWithoutSignature.remove("signature");
            String bodyForVerification = objectMapper.writeValueAsString(bodyWithoutSignature);
            
            // Verify signature
            String expectedSignature = generateHmacSha256(bodyForVerification, config.getChecksumKey());
            if (!signature.equals(expectedSignature)) {
                log.warn("Webhook signature verification failed");
                return "01"; // Invalid signature
            }
            
            // Extract code from webhook data
            String code = rootNode.has("code") ? rootNode.get("code").asText() : "00";
            return code;
        } catch (Exception e) {
            log.error("Webhook verification failed: {}", e.getMessage(), e);
            return "01";
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
