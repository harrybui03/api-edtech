package com.example.backend.service;

import com.example.backend.constant.TransactionStatus;
import com.example.backend.dto.response.payment.PaymentResponse;
import com.example.backend.dto.response.payment.TransactionListResponse;
import com.example.backend.entity.*;
import com.example.backend.repository.CourseRepository;
import com.example.backend.repository.BatchRepository;
import com.example.backend.repository.PayOSConfigRepository;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.excecption.DataNotFoundException;
import com.example.backend.excecption.InvalidRequestDataException;
import org.springframework.security.core.context.SecurityContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.backend.entity.Batch;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final PayOSIntegrationService payOSIntegrationService;
    private final PayOSConfigRepository payOSConfigRepository;
    private final EnrollmentService enrollmentService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final BatchRepository batchRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private final Random random = new Random();

    /**
     * Generates a unique random order code between 1000 and 10000000
     * @return unique order code
     */
    private Long generateUniqueOrderCode() {
        Long orderCode;
        int maxAttempts = 100; // Prevent infinite loop
        int attempts = 0;
        
        do {
            // Generate random number between 1000 and 10000000
            orderCode = (long) (1000 + random.nextInt(10000000 - 1000 + 1));
            attempts++;
            
            if (attempts >= maxAttempts) {
                throw new RuntimeException("Unable to generate unique order code after " + maxAttempts + " attempts");
            }
        } while (transactionRepository.findByOrderCode(orderCode).isPresent());
        
        return orderCode;
    }

    @Transactional
    public PaymentResponse createPaymentBySlug(String courseSlug) {
        // Get current user
        User student = getCurrentUser();
        log.info("Creating payment for student: {} and course slug: {}", student.getId(), courseSlug);

        // Validate course exists and is paid
        Course course = courseRepository.findBySlug(courseSlug)
                .orElseThrow(() -> new DataNotFoundException("Course not found with slug: " + courseSlug));

        if (!Boolean.TRUE.equals(course.getPaidCourse())) {
            throw new InvalidRequestDataException("Course is not a paid course");
        }

        if (course.getSellingPrice() == null || course.getSellingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestDataException("Course price is not set or invalid");
        }

        // Get course instructor
        User instructor = course.getInstructors().stream()
                .findFirst()
                .map(CourseInstructor::getUser)
                .orElseThrow(() -> new DataNotFoundException("No instructor found for course slug: " + courseSlug));

        // Check if student already enrolled (paid)
        if (transactionRepository.countPaidTransactionsByStudentAndCourse(student.getId(), course.getId()) > 0) {
            throw new InvalidRequestDataException("Student is already enrolled in this course");
        }

        // Get instructor's PayOS config
        PayOSConfig payOSConfig = payOSConfigRepository.findByInstructorId(instructor.getId())
                .orElseThrow(() -> new DataNotFoundException("No PayOS configuration found for instructor: " + instructor.getId()));

        // Generate unique random order code between 1000 and 10000000
        Long generatedOrderCode = generateUniqueOrderCode();
        String returnUrlPrefix = baseUrl + "/payment/success?orderCode=";
        String cancelUrlPrefix = baseUrl + "/payment/cancel?orderCode=";

        Transaction transaction = Transaction.builder()
                .orderCode(generatedOrderCode)
                .student(student)
                .instructor(instructor)
                .course(course)
                .amount(course.getSellingPrice())
                .currency(course.getCurrency() != null ? course.getCurrency() : "VND")
                .status(TransactionStatus.PENDING)
                .description("Payment for course: " + course.getTitle())
                .accountNumber(payOSConfig.getAccountNumber())
                .webhookReceived(false)
                .build();

        transaction = transactionRepository.save(transaction);
        transaction = transactionRepository.findById(transaction.getId())
                .orElseThrow(() -> new DataNotFoundException("Transaction not found after save"));

        String orderCode = String.valueOf(transaction.getOrderCode());
        transaction.setReturnUrl(returnUrlPrefix + orderCode);
        transaction.setCancelUrl(cancelUrlPrefix + orderCode);
        transaction = transactionRepository.save(transaction);

        try {
            PayOSIntegrationService.PayOSPaymentResponse payOSResponse = payOSIntegrationService.createPaymentRequest(payOSConfig, transaction);

            if (payOSResponse.getData() != null) {
                transaction.setPaymentId(payOSResponse.getData().getPaymentId());
                transaction.setPaymentUrl(payOSResponse.getData().getPaymentUrl());
                transaction = transactionRepository.save(transaction);
                log.info("Payment created successfully for order: {} with PayOS data", orderCode);
            } else {
                log.warn("PayOS response has no data for order: {}, code: {}, message: {}",
                        orderCode, payOSResponse.getCode(), payOSResponse.getMessage());
            }

            PaymentResponse response = mapToResponse(transaction);
            if (payOSResponse.getData() != null) {
                response.setQrCode(payOSResponse.getData().getQrCode());
            }
            return response;
        } catch (Exception e) {
            log.error("Error creating PayOS payment request: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create payment request: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentResponse createBatchPaymentBySlug(String batchSlug) {
        // Validate batch
        Batch batch = batchRepository.findBySlug(batchSlug)
                .orElseThrow(() -> new DataNotFoundException("Batch not found with slug: " + batchSlug));

        if (!batch.isPaidBatch()) {
            throw new InvalidRequestDataException("Batch is not a paid batch");
        }

        // Get current user
        User student = getCurrentUser();

        // Prevent duplicate paid enrollment
        if (transactionRepository.countPaidTransactionsByStudentAndBatch(student.getId(), batch.getId()) > 0) {
            throw new InvalidRequestDataException("Student is already enrolled in this batch");
        }

        // Validate price
        if (batch.getSellingPrice() == null || batch.getSellingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestDataException("Batch price is not set or invalid");
        }

        // Get any instructor of batch (mirror course logic)
        User instructor = batch.getInstructors().stream()
                .findFirst()
                .map(BatchInstructor::getInstructor)
                .orElseThrow(() -> new DataNotFoundException("No instructor found for batch slug: " + batchSlug));

        // Get instructor's PayOS config
        PayOSConfig payOSConfig = payOSConfigRepository.findByInstructorId(instructor.getId())
                .orElseThrow(() -> new DataNotFoundException("No PayOS configuration found for instructor: " + instructor.getId()));

        // Generate unique random order code between 1000 and 10000000
        Long generatedOrderCode = generateUniqueOrderCode();
        String returnUrlPrefix = baseUrl + "/payment/success?orderCode=";
        String cancelUrlPrefix = baseUrl + "/payment/cancel?orderCode=";

        Transaction transaction = Transaction.builder()
                .orderCode(generatedOrderCode)
                .student(student)
                .instructor(instructor)
                .batch(batch)
                .amount(batch.getSellingPrice())
                .currency("VND")
                .status(TransactionStatus.PENDING)
                .description("Payment for batch: " + batch.getTitle())
                .accountNumber(payOSConfig.getAccountNumber())
                .webhookReceived(false)
                .build();

        transaction = transactionRepository.save(transaction);
        transaction = transactionRepository.findById(transaction.getId())
                .orElseThrow(() -> new DataNotFoundException("Transaction not found after save"));

        String orderCode = String.valueOf(transaction.getOrderCode());
        transaction.setReturnUrl(returnUrlPrefix + orderCode);
        transaction.setCancelUrl(cancelUrlPrefix + orderCode);
        transaction = transactionRepository.save(transaction);

        try {
            PayOSIntegrationService.PayOSPaymentResponse payOSResponse = payOSIntegrationService.createPaymentRequest(payOSConfig, transaction);

            if (payOSResponse.getData() != null) {
                transaction.setPaymentId(payOSResponse.getData().getPaymentId());
                transaction.setPaymentUrl(payOSResponse.getData().getPaymentUrl());
                transaction = transactionRepository.save(transaction);
                log.info("Batch payment created successfully for order: {} with PayOS data", orderCode);
            } else {
                log.warn("PayOS response has no data for order: {}, code: {}, message: {}",
                        orderCode, payOSResponse.getCode(), payOSResponse.getMessage());
            }

            PaymentResponse response = mapToResponse(transaction);
            if (payOSResponse.getData() != null) {
                response.setQrCode(payOSResponse.getData().getQrCode());
            }
            return response;
        } catch (Exception e) {
            log.error("Error creating PayOS payment request: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create payment request: " + e.getMessage());
        }
    }

    public PaymentResponse getPaymentStatus(String orderCode) {
        log.info("Getting payment status for order: {}", orderCode);

        Long numericOrderCode;
        try {
            numericOrderCode = Long.valueOf(orderCode);
        } catch (NumberFormatException ex) {
            throw new DataNotFoundException("Invalid order code format");
        }

        Transaction transaction = transactionRepository.findByOrderCode(numericOrderCode)
                .orElseThrow(() -> new DataNotFoundException("Transaction not found with order code: " + orderCode));

        return mapToResponse(transaction);
    }

    @Transactional
    public void handlePayOSWebhook(String rawBody, String signature) {
        log.info("Handling PayOS webhook raw body");

        try {
            // Verify webhook using SDK via integration service (use any config to decode)
            PayOSConfig anyConfig = payOSConfigRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new DataNotFoundException("No PayOS configuration available"));
            String sdkCode = payOSIntegrationService.verifyWebhookAndGetCode(rawBody, anyConfig);

            // Extract orderCode from raw body
            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(rawBody);
            Long oc = node.path("data").path("orderCode").asLong();

            // Find transaction
            Transaction transaction = transactionRepository.findByOrderCode(oc)
                    .orElseThrow(() -> new DataNotFoundException("Not Found"));

            if ("00".equals(sdkCode)) {
                transaction.setStatus(TransactionStatus.PAID);
                transaction.setPaidAt(OffsetDateTime.now());
                if (transaction.getCourse() != null) {
                    enrollmentService.createEnrollment(transaction.getStudent().getId(), transaction.getCourse().getId());
                } else if (transaction.getBatch() != null) {
                    // Batch enrollment after payment success
                    enrollmentService.enrollInBatchBySlug(transaction.getBatch().getSlug());
                }
                sendPaymentSuccessNotifications(transaction);
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setFailedAt(OffsetDateTime.now());
                sendPaymentFailureNotification(transaction);
            }

            transaction.setWebhookReceived(true);
            transaction.setWebhookSignature(signature);
            transactionRepository.save(transaction);

            log.info("Webhook processed successfully for order: {}", oc);

        } catch (Exception e) {
            log.error("Error processing PayOS webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing webhook: " + e.getMessage());
        }
    }

    public TransactionListResponse getTransactions(String userType, String status, UUID userId, int page, int size, String sort) {
        log.info("Getting transactions with userType: {}, status: {}, userId: {}, page: {}, size: {}", userType, status, userId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transaction> transactionPage;

        if ("student".equals(userType)) {
            if (status != null) {
                TransactionStatus transactionStatus = TransactionStatus.valueOf(status.toUpperCase());
                transactionPage = transactionRepository.findByStudentIdAndStatus(userId, transactionStatus, pageable);
            } else {
                transactionPage = transactionRepository.findByStudentId(userId, pageable);
            }
        } else if ("instructor".equals(userType)) {
            if (status != null) {
                TransactionStatus transactionStatus = TransactionStatus.valueOf(status.toUpperCase());
                transactionPage = transactionRepository.findByInstructorIdAndStatus(userId, transactionStatus, pageable);
            } else {
                transactionPage = transactionRepository.findByInstructorId(userId, pageable);
            }
        } else {
            // Admin view - all transactions
            if (status != null) {
                TransactionStatus transactionStatus = TransactionStatus.valueOf(status.toUpperCase());
                transactionPage = transactionRepository.findByStatus(transactionStatus, pageable);
            } else {
                transactionPage = transactionRepository.findAll(pageable);
            }
        }

        List<TransactionListResponse.TransactionSummaryResponse> content = transactionPage.getContent().stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());

        return TransactionListResponse.builder()
                .content(content)
                .pagination(createPaginationResponse(transactionPage))
                .build();
    }


    private PaymentResponse mapToResponse(Transaction transaction) {
        return PaymentResponse.builder()
                .id(transaction.getId())
                .orderCode(String.valueOf(transaction.getOrderCode()))
                .studentId(transaction.getStudent().getId())
                .studentName(transaction.getStudent().getFullName())
                .instructorId(transaction.getInstructor().getId())
                .instructorName(transaction.getInstructor().getFullName())
                .courseId(transaction.getCourse() != null ? transaction.getCourse().getId() : null)
                .courseTitle(transaction.getCourse() != null ? transaction.getCourse().getTitle() : null)
                .batchId(transaction.getBatch() != null ? transaction.getBatch().getId() : null)
                .batchTitle(transaction.getBatch() != null ? transaction.getBatch().getTitle() : null)
                .paymentId(transaction.getPaymentId())
                .paymentUrl(transaction.getPaymentUrl())
                .qrCode(null)
                .accountNumber(transaction.getAccountNumber())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .returnUrl(transaction.getReturnUrl())
                .cancelUrl(transaction.getCancelUrl())
                .paidAt(transaction.getPaidAt())
                .failedAt(transaction.getFailedAt())
                .webhookReceived(transaction.getWebhookReceived())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }

    private TransactionListResponse.TransactionSummaryResponse mapToSummaryResponse(Transaction transaction) {
        String title = transaction.getCourse() != null
                ? transaction.getCourse().getTitle()
                : (transaction.getBatch() != null ? transaction.getBatch().getTitle() : null);
        return TransactionListResponse.TransactionSummaryResponse.builder()
                .id(transaction.getId().toString())
                .orderCode(String.valueOf(transaction.getOrderCode()))
                .studentName(transaction.getStudent().getFullName())
                .instructorName(transaction.getInstructor().getFullName())
                .courseTitle(title)
                .amount(transaction.getAmount().toString())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus().toString())
                .createdAt(transaction.getCreatedAt().toString())
                .paidAt(transaction.getPaidAt() != null ? transaction.getPaidAt().toString() : null)
                .build();
    }

    private com.example.backend.dto.response.pagination.PaginationMetadata createPaginationResponse(Page<Transaction> page) {
        return new com.example.backend.dto.response.pagination.PaginationMetadata(page);
    }

    private void sendPaymentSuccessNotifications(Transaction transaction) {
        // Send email to student
        emailService.sendPaymentSuccessEmail(transaction.getStudent().getEmail(), transaction);
        
        // Send email to instructor
        emailService.sendPaymentReceivedEmail(transaction.getInstructor().getEmail(), transaction);
    }

    private void sendPaymentFailureNotification(Transaction transaction) {
        // Send email to student
        emailService.sendPaymentFailureEmail(transaction.getStudent().getEmail(), transaction);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("User not found with email: " + email));
    }
}
