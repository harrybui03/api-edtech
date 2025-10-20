package com.example.backend.controller;

import com.example.backend.dto.model.BatchDiscussionDto;
import com.example.backend.dto.request.discussion.CreateDiscussionRequest;
import com.example.backend.dto.request.discussion.UpdateDiscussionRequest;
import com.example.backend.dto.response.pagination.PaginationResponse;
import com.example.backend.service.BatchDiscussionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/discussions")
@RequiredArgsConstructor
public class BatchDiscussionController {

    private final BatchDiscussionService discussionService;

    @PostMapping("/batch/{batchId}")
    @Operation(summary = "Create a new discussion or reply in a batch")
    public ResponseEntity<BatchDiscussionDto> createDiscussion(@PathVariable UUID batchId, @Valid @RequestBody CreateDiscussionRequest request) {
        return new ResponseEntity<>(discussionService.createDiscussion(batchId, request), HttpStatus.CREATED);
    }

    @PutMapping("/{discussionId}")
    @Operation(summary = "Update a discussion")
    public ResponseEntity<BatchDiscussionDto> updateDiscussion(@PathVariable UUID discussionId, @Valid @RequestBody UpdateDiscussionRequest request) {
        return ResponseEntity.ok(discussionService.updateDiscussion(discussionId, request));
    }

    @DeleteMapping("/{discussionId}")
    @Operation(summary = "Delete a discussion")
    public ResponseEntity<Void> deleteDiscussion(@PathVariable UUID discussionId) {
        discussionService.deleteDiscussion(discussionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/batch/{batchId}")
    @Operation(summary = "Get top-level discussions for a batch")
    public ResponseEntity<PaginationResponse<BatchDiscussionDto>> getDiscussionsForBatch(@PathVariable UUID batchId, Pageable pageable) {
        Page<BatchDiscussionDto> discussions = discussionService.getDiscussionsForBatch(batchId, pageable);
        return ResponseEntity.ok(new PaginationResponse<>(discussions));
    }

    @GetMapping("/{discussionId}/replies")
    @Operation(summary = "Get replies for a specific discussion")
    public ResponseEntity<PaginationResponse<BatchDiscussionDto>> getRepliesForDiscussion(@PathVariable UUID discussionId, Pageable pageable) {
        Page<BatchDiscussionDto> replies = discussionService.getRepliesForDiscussion(discussionId, pageable);
        return ResponseEntity.ok(new PaginationResponse<>(replies));
    }
}
