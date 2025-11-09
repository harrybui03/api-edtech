package com.example.backend.repository;

import com.example.backend.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    boolean existsByMemberIdAndCourseId(UUID memberId, UUID courseId);

    Optional<Enrollment> findByMemberIdAndCourseId(UUID memberId, UUID courseId);

    List<Enrollment> findByMemberId(UUID memberId);

    @Query("SELECT e FROM Enrollment e JOIN e.course c JOIN c.instructors ci WHERE ci.user.id = :instructorId AND c.id = :courseId")
    List<Enrollment> findByCourseIdAndInstructorId(@Param("courseId") UUID courseId, @Param("instructorId") UUID instructorId);

    @Query("SELECT e FROM Enrollment e WHERE e.member.id = :memberId")
    Page<Enrollment> findByMemberId(@Param("memberId") UUID memberId, Pageable pageable);
}
