package com.example.backend.repository;

import com.example.backend.entity.PayOSConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayOSConfigRepository extends JpaRepository<PayOSConfig, UUID> {

    @Query("SELECT p FROM PayOSConfig p WHERE p.instructor.id = :instructorId")
    Optional<PayOSConfig> findByInstructorId(@Param("instructorId") UUID instructorId);

    @Query("SELECT COUNT(p) > 0 FROM PayOSConfig p WHERE p.instructor.id = :instructorId AND p.isActive = true")
    boolean existsActiveByInstructorId(@Param("instructorId") UUID instructorId);
}
