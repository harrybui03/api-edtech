package com.example.backend.service;

import com.example.backend.dto.request.payos.CreatePayOSConfigRequest;
import com.example.backend.dto.response.payos.PayOSConfigResponse;
import com.example.backend.entity.PayOSConfig;
import com.example.backend.entity.User;
import com.example.backend.excecption.DataNotFoundException;
import com.example.backend.excecption.InvalidRequestDataException;
import com.example.backend.repository.PayOSConfigRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.backend.excecption.ResourceNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSConfigService {

    private final PayOSConfigRepository payOSConfigRepository;
    private final UserRepository userRepository;

    @Transactional
    public PayOSConfigResponse createPayOSConfig(CreatePayOSConfigRequest request) {
        User currentUser = getCurrentUser();
        // Validate instructor exists
        User instructor = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new DataNotFoundException("Instructor not found with id: " + currentUser.getId()));

        // Check if instructor already has an active config
        if (payOSConfigRepository.existsActiveByInstructorId(currentUser.getId())) {
            throw new InvalidRequestDataException("Instructor already has an active PayOS configuration");
        }

        // Deactivate any existing configs for this instructor
        payOSConfigRepository.findByInstructorId(currentUser.getId())
                .ifPresent(existingConfig -> {
                    existingConfig.setIsActive(false);
                    payOSConfigRepository.save(existingConfig);
                });

        // Create new config
        PayOSConfig config = PayOSConfig.builder()
                .instructor(instructor)
                .clientId(request.getClientId())
                .apiKey(request.getApiKey())
                .checksumKey(request.getChecksumKey())
                .accountNumber(request.getAccountNumber())
                .isActive(true)
                .build();

        PayOSConfig savedConfig = payOSConfigRepository.save(config);
        log.info("PayOS config created successfully with id: {}", savedConfig.getId());

        return mapToResponse(savedConfig);
    }

    public PayOSConfigResponse getPayOSConfigByInstructorId(UUID instructorId) {
        log.info("Getting PayOS config for instructor: {}", instructorId);

        PayOSConfig config = payOSConfigRepository.findByInstructorId(instructorId)
                .orElseThrow(() -> new DataNotFoundException("No active PayOS configuration found for instructor: " + instructorId));

        return mapToResponse(config);
    }

    public PayOSConfigResponse getMyPayOSConfig() {
        User currentUser = getCurrentUser();
        return getPayOSConfigByInstructorId(currentUser.getId());
    }
    

    private PayOSConfigResponse mapToResponse(PayOSConfig config) {
        return PayOSConfigResponse.builder()
                .id(config.getId())
                .instructorId(config.getInstructor().getId())
                .instructorName(config.getInstructor().getFullName())
                .clientId(config.getClientId())
                .accountNumber(config.getAccountNumber())
                .isActive(config.getIsActive())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found."));
    }
}
