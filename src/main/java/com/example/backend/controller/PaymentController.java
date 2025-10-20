package com.example.backend.controller;

import com.example.backend.dto.response.payment.PaymentResponse;
import com.example.backend.dto.response.payment.TransactionListResponse;
import com.example.backend.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "APIs for payment processing")
public class PaymentController {

    private final PaymentService paymentService;


    @GetMapping("/status/{orderCode}")
    @Operation(summary = "Get payment status", description = "Get payment status by order code")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable String orderCode) {
        PaymentResponse response = paymentService.getPaymentStatus(orderCode);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook/payos")
    public ResponseEntity<String> handlePayOSWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "x-payos-signature", required = false) String signature) {
		try {
			paymentService.handlePayOSWebhook(rawBody, signature);
			return ResponseEntity.ok("Webhook processed successfully");
		} catch (Exception e) {
			// Always acknowledge webhook with 200, even if internal processing fails
			return ResponseEntity.ok("Webhook received");
		}
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get transactions", description = "Get paginated list of transactions")
    public ResponseEntity<TransactionListResponse> getTransactions(
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            Authentication authentication) {
        
        UUID userId = UUID.fromString(authentication.getName());
        TransactionListResponse response = paymentService.getTransactions(userType, status, userId, page, size, sort);
        return ResponseEntity.ok(response);
    }
}
