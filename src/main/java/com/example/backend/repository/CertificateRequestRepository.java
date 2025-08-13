package com.example.backend.repository;

import com.example.backend.entity.CertificateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CertificateRequestRepository extends JpaRepository<CertificateRequest, UUID> {
} 