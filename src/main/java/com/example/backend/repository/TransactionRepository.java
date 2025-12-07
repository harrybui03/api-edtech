package com.example.backend.repository;

import com.example.backend.constant.TransactionStatus;
import com.example.backend.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.backend.dto.response.statistics.RevenueDataPoint;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

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
    Page<Transaction> findByStatus(@Param("status") TransactionStatus status, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.student.id = :studentId AND t.status = :status")
    Page<Transaction> findByStudentIdAndStatus(@Param("studentId") UUID studentId, 
                                               @Param("status") TransactionStatus status,
                                               Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.instructor.id = :instructorId AND t.status = :status")
    Page<Transaction> findByInstructorIdAndStatus(@Param("instructorId") UUID instructorId, 
                                                  @Param("status") TransactionStatus status,
                                                  Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.course.id = :courseId")
    Page<Transaction> findByCourseId(@Param("courseId") UUID courseId, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.student.id = :studentId AND t.course.id = :courseId AND t.status = 'PAID'")
    long countPaidTransactionsByStudentAndCourse(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.student.id = :studentId AND t.batch.id = :batchId AND t.status = 'PAID'")
    long countPaidTransactionsByStudentAndBatch(@Param("studentId") UUID studentId, @Param("batchId") UUID batchId);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.instructor.id = :instructorId " +
            "AND t.status = :status AND t.course IS NOT NULL")
    BigDecimal sumPaidCourseRevenueByInstructor(@Param("instructorId") UUID instructorId, @Param("status") TransactionStatus status);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.instructor.id = :instructorId " +
            "AND t.status = :status AND t.batch IS NOT NULL")
    BigDecimal sumPaidBatchRevenueByInstructor(@Param("instructorId") UUID instructorId, @Param("status") TransactionStatus status);

    @Query(value = "WITH GroupedTransactions AS (" +
            "    SELECT DATE_TRUNC(:groupBy, t.paid_at) as truncated_date, SUM(t.amount) as total_revenue " +
            "    FROM transactions t " +
            "    WHERE t.instructor_id = :instructorId " +
            "      AND t.status = 'PAID' " +
            "      AND t.course_id IS NOT NULL " +
            "      AND t.paid_at >= :startDate AND t.paid_at < :endDate " +
            "    GROUP BY truncated_date " +
            ") " +
            "SELECT TO_CHAR(gt.truncated_date, :dateFormat) as date, gt.total_revenue as revenue " +
            "FROM GroupedTransactions gt " +
            "ORDER BY gt.truncated_date ASC", nativeQuery = true)
    List<RevenueDataPoint> getCourseRevenueGroupedByDate(@Param("instructorId") UUID instructorId, @Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, @Param("dateFormat") String dateFormat, @Param("groupBy") String groupBy);

    @Query(value = "WITH GroupedTransactions AS (" +
            "    SELECT DATE_TRUNC(:groupBy, t.paid_at) as truncated_date, SUM(t.amount) as total_revenue " +
            "    FROM transactions t " +
            "    WHERE t.instructor_id = :instructorId " +
            "      AND t.status = 'PAID' " +
            "      AND t.batch_id IS NOT NULL " +
            "      AND t.paid_at >= :startDate AND t.paid_at < :endDate " +
            "    GROUP BY truncated_date " +
            ") " +
            "SELECT TO_CHAR(gt.truncated_date, :dateFormat) as date, gt.total_revenue as revenue " +
            "FROM GroupedTransactions gt " +
            "ORDER BY gt.truncated_date ASC", nativeQuery = true)
    List<RevenueDataPoint> getBatchRevenueGroupedByDate(@Param("instructorId") UUID instructorId, @Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, @Param("dateFormat") String dateFormat, @Param("groupBy") String groupBy);
}