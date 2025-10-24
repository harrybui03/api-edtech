package com.example.backend.service;

import com.example.backend.dto.request.payos.CreatePayOSConfigRequest;
import com.example.backend.dto.request.payos.UpdatePayOSConfigRequest;
import com.example.backend.dto.response.payos.PayOSConfigResponse;
import com.example.backend.entity.PayOSConfig;
import com.example.backend.entity.User;
import com.example.backend.excecption.DataNotFoundException;
import com.example.backend.excecption.ForbiddenException;
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

        // Deactivate any existing active configs for this instructor
        payOSConfigRepository.findByInstructorId(currentUser.getId())
                .ifPresent(existingConfig -> {
                    if (Boolean.TRUE.equals(existingConfig.getIsActive())) {
                        existingConfig.setIsActive(false);
                        payOSConfigRepository.save(existingConfig);
                        log.info("Deactivated existing PayOS config for instructor: {}", currentUser.getId());
                    }
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
    
    @Transactional
    public PayOSConfigResponse updatePayOSConfig(UUID configId, UpdatePayOSConfigRequest request) {
        User currentUser = getCurrentUser();

        PayOSConfig config = payOSConfigRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("PayOS configuration not found with id: " + configId));

        // Verify ownership
        if (!config.getInstructor().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not authorized to update this PayOS configuration.");
        }

        // Update fields if provided
        if (request.getClientId() != null) {
            config.setClientId(request.getClientId());
        }
        if (request.getApiKey() != null) {
            config.setApiKey(request.getApiKey());
        }
        if (request.getChecksumKey() != null) {
            config.setChecksumKey(request.getChecksumKey());
        }
        if (request.getAccountNumber() != null) {
            config.setAccountNumber(request.getAccountNumber());
        }
        if (request.getIsActive() != null) {
            config.setIsActive(request.getIsActive());
        }

        PayOSConfig updatedConfig = payOSConfigRepository.save(config);

        return mapToResponse(updatedConfig);
    }

    private PayOSConfigResponse mapToResponse(PayOSConfig config) {
        return PayOSConfigResponse.builder()
                .id(config.getId())
                .instructorId(config.getInstructor().getId())
                .instructorName(config.getInstructor().getFullName())
                .clientId(config.getClientId())
                .apiKey(config.getApiKey())
                .checksumKey(config.getChecksumKey())
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
