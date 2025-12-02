package com.example.backend.service;

import com.example.backend.constant.BatchStatus;
import com.example.backend.constant.CourseStatus;
import com.example.backend.constant.TransactionStatus;
import com.example.backend.dto.response.statistics.RevenueDataPoint;
import com.example.backend.dto.response.statistics.RevenueOverTimeResponse;
import com.example.backend.dto.response.statistics.InstructorStatsResponse;
import com.example.backend.dto.response.statistics.PerformanceReportItem;
import com.example.backend.entity.User;
import com.example.backend.excecption.InvalidRequestDataException;
import com.example.backend.excecption.ResourceNotFoundException;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public InstructorStatsResponse getInstructorOverviewStats() {
        User currentUser = getCurrentUser();
        UUID instructorId = currentUser.getId();

        long totalCourses = courseRepository.countByInstructors_User_IdAndStatus(instructorId, CourseStatus.PUBLISHED);
        long totalBatches = batchRepository.countByInstructors_Instructor_IdAndStatus(instructorId, BatchStatus.PUBLISHED);

        BigDecimal courseRevenue = transactionRepository.sumPaidCourseRevenueByInstructor(instructorId, TransactionStatus.PAID);
        BigDecimal batchRevenue = transactionRepository.sumPaidBatchRevenueByInstructor(instructorId, TransactionStatus.PAID);

        return InstructorStatsResponse.builder()
                .totalCoursePublished(totalCourses)
                .totalBatchPublished(totalBatches)
                .courseRevenue(courseRevenue != null ? courseRevenue : BigDecimal.ZERO)
                .batchRevenue(batchRevenue != null ? batchRevenue : BigDecimal.ZERO)
                .build();
    }

    @Transactional(readOnly = true)
    public RevenueOverTimeResponse getRevenueOverTime(String period, String type) {
        User currentUser = getCurrentUser();
        UUID instructorId = currentUser.getId();

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime startDate;
        OffsetDateTime endDate = now.plusDays(1); // exclusive
        String groupBy;
        String dateFormat;

        switch (period.toUpperCase()) {
            case "WEEK":
                startDate = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0).withNano(0);
                groupBy = "DAY";
                dateFormat = "YYYY-MM-DD";
                break;
            case "MONTH":
                startDate = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
                groupBy = "DAY";
                dateFormat = "YYYY-MM-DD";
                break;
            case "YEAR":
                startDate = now.with(TemporalAdjusters.firstDayOfYear()).withHour(0).withMinute(0).withSecond(0).withNano(0);
                groupBy = "MONTH";
                dateFormat = "YYYY-MM";
                break;
            case "ALL_TIME":
            default:
                startDate = now.minusYears(5);
                groupBy = "YEAR";
                dateFormat = "YYYY";
                period = "ALL_TIME"; // Normalize
                break;
        }

        List<RevenueDataPoint> dataPoints;
        if ("COURSE".equalsIgnoreCase(type)) {
            dataPoints = transactionRepository.getCourseRevenueGroupedByDate(instructorId, startDate, endDate, dateFormat, groupBy.toLowerCase());
        } else if ("BATCH".equalsIgnoreCase(type)) {
            dataPoints = transactionRepository.getBatchRevenueGroupedByDate(instructorId, startDate, endDate, dateFormat, groupBy.toLowerCase());
        } else {
            throw new InvalidRequestDataException("Invalid type specified. Must be 'COURSE' or 'BATCH'.");
        }

        return RevenueOverTimeResponse.builder()
                .type(type.toUpperCase())
                .period(period.toUpperCase())
                .groupBy(groupBy)
                .currency("VND")
                .dataPoints(dataPoints)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<PerformanceReportItem> getCoursePerformanceReport(Pageable pageable) {
        User currentUser = getCurrentUser();
        UUID instructorId = currentUser.getId();
        return courseRepository.getCoursePerformanceReport(instructorId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PerformanceReportItem> getBatchPerformanceReport(Pageable pageable) {
        User currentUser = getCurrentUser();
        UUID instructorId = currentUser.getId();
        return batchRepository.getBatchPerformanceReport(instructorId, pageable);
    }


    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found."));
    }
}
