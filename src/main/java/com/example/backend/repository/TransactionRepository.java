package com.example.backend.repository;

import com.example.backend.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByOrderCode(Long orderCode);

    @Query("SELECT t FROM Transaction t WHERE t.orderCode = :orderCode")
    Optional<Transaction> findByOrderCodeWithDetails(@Param("orderCode") Long orderCode);

    @Query(value = "SELECT nextval('order_code_seq')", nativeQuery = true)
    Long nextOrderCode();

    @Query("SELECT t FROM Transaction t WHERE t.student.id = :studentId")
    Page<Transaction> findByStudentId(@Param("studentId") UUID studentId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.instructor.id = :instructorId")
    Page<Transaction> findByInstructorId(@Param("instructorId") UUID instructorId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.status = :status")
    Page<Transaction> findByStatus(@Param("status") Transaction.TransactionStatus status, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.student.id = :studentId AND t.status = :status")
    Page<Transaction> findByStudentIdAndStatus(@Param("studentId") UUID studentId, 
                                               @Param("status") Transaction.TransactionStatus status, 
                                               Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.instructor.id = :instructorId AND t.status = :status")
    Page<Transaction> findByInstructorIdAndStatus(@Param("instructorId") UUID instructorId, 
                                                  @Param("status") Transaction.TransactionStatus status, 
                                                  Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.course.id = :courseId")
    Page<Transaction> findByCourseId(@Param("courseId") UUID courseId, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.student.id = :studentId AND t.course.id = :courseId AND t.status = 'PAID'")
    long countPaidTransactionsByStudentAndCourse(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId);
}
