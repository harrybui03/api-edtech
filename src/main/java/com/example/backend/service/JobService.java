package com.example.backend.service;

import com.example.backend.dto.model.JobDto;
import com.example.backend.entity.Job;
import com.example.backend.entity.User;
import com.example.backend.excecption.ResourceNotFoundException;
import com.example.backend.repository.JobRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<JobDto> getMyJobs(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Job> jobs = jobRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable);
        return jobs.map(this::toDto);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found."));
    }

    private JobDto toDto(Job job) {
        return new JobDto(
                job.getId(),
                job.getJobType(),
                job.getStatus(),
                job.getEntityId(),
                job.getEntityType(),
                job.getCreatedAt()
        );
    }
}