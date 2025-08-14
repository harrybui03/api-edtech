package com.example.backend.repository;

import com.example.backend.entity.BatchEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BatchEnrollmentRepository extends JpaRepository<BatchEnrollment, UUID> {}