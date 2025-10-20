package com.example.backend.service;

import com.example.backend.dto.model.BatchDiscussionDto;
import com.example.backend.dto.request.discussion.CreateDiscussionRequest;
import com.example.backend.dto.request.discussion.UpdateDiscussionRequest;
import com.example.backend.entity.Batch;
import com.example.backend.entity.BatchDiscussion;
import com.example.backend.entity.User;
import com.example.backend.excecption.ForbiddenException;
import com.example.backend.excecption.ResourceNotFoundException;
import com.example.backend.mapper.BatchDiscussionMapper;
import com.example.backend.repository.BatchDiscussionRepository;
import com.example.backend.repository.BatchRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BatchDiscussionService {

    private final BatchDiscussionRepository discussionRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;
    private final BatchDiscussionMapper discussionMapper;

    @Transactional
    public BatchDiscussionDto createDiscussion(UUID batchId, CreateDiscussionRequest request) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + batchId));
        User currentUser = getCurrentUser();

        BatchDiscussion discussion = new BatchDiscussion();
        discussion.setBatch(batch);
        discussion.setUser(currentUser);
        discussion.setTitle(request.getTitle());
        discussion.setContent(request.getContent());
        discussion.setCreatedAt(LocalDateTime.now());

        if (request.getReplyToId() != null) {
            BatchDiscussion parentDiscussion = discussionRepository.findById(request.getReplyToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent discussion not found with id: " + request.getReplyToId()));
            discussion.setReplyTo(parentDiscussion);
        }

        BatchDiscussion savedDiscussion = discussionRepository.save(discussion);
        return discussionMapper.toDto(savedDiscussion, new ArrayList<>(savedDiscussion.getDocuments()));
    }

    @Transactional
    public BatchDiscussionDto updateDiscussion(UUID discussionId, UpdateDiscussionRequest request) {
        BatchDiscussion discussion = findDiscussionById(discussionId);
        checkOwnership(discussion);

        discussion.setTitle(request.getTitle());
        discussion.setContent(request.getContent());

        BatchDiscussion updatedDiscussion = discussionRepository.save(discussion);
        return discussionMapper.toDto(updatedDiscussion, new ArrayList<>(updatedDiscussion.getDocuments()));
    }

    @Transactional
    public void deleteDiscussion(UUID discussionId) {
        BatchDiscussion discussion = findDiscussionById(discussionId);
        checkOwnership(discussion);
        discussionRepository.delete(discussion);
    }

    @Transactional(readOnly = true)
    public Page<BatchDiscussionDto> getDiscussionsForBatch(UUID batchId, Pageable pageable) {
        if (!batchRepository.existsById(batchId)) {
            throw new ResourceNotFoundException("Batch not found with id: " + batchId);
        }
        Page<BatchDiscussion> discussions = discussionRepository.findByBatchIdAndReplyToIsNullOrderByCreatedAtDesc(batchId, pageable);
        return discussions.map(discussion -> discussionMapper.toDto(discussion, new ArrayList<>(discussion.getDocuments())));
    }

    @Transactional(readOnly = true)
    public Page<BatchDiscussionDto> getRepliesForDiscussion(UUID discussionId, Pageable pageable) {
        if (!discussionRepository.existsById(discussionId)) {
            throw new ResourceNotFoundException("Discussion not found with id: " + discussionId);
        }
        Page<BatchDiscussion> replies = discussionRepository.findByReplyToIdOrderByCreatedAtAsc(discussionId, pageable);
        return replies.map(reply -> discussionMapper.toDto(reply, new ArrayList<>(reply.getDocuments())));
    }

    private BatchDiscussion findDiscussionById(UUID discussionId) {
        return discussionRepository.findById(discussionId)
                .orElseThrow(() -> new ResourceNotFoundException("Discussion not found with id: " + discussionId));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found."));
    }

    private void checkOwnership(BatchDiscussion discussion) {
        User currentUser = getCurrentUser();
        if (!discussion.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You do not have permission to modify this discussion.");
        }
    }
}