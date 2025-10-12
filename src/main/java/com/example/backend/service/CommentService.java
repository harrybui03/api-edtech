package com.example.backend.service;

import com.example.backend.dto.request.comment.CommentRequest;
import com.example.backend.dto.response.comment.CommentResponse;
import com.example.backend.dto.response.comment.VoteResponse;
import com.example.backend.entity.*;
import com.example.backend.excecption.ForbiddenException;
import com.example.backend.excecption.ResourceNotFoundException;
import com.example.backend.mapper.CommentMapper;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public CommentResponse createComment(UUID lessonId, CommentRequest request) {
        User currentUser = getCurrentUser();
        Lesson lesson = findLessonById(lessonId);
        
        // Verify user has access to the lesson (enrolled in course)
        verifyLessonAccess(lesson, currentUser.getId());

        Comment comment = CommentMapper.toEntity(request);
        comment.setLesson(lesson);
        comment.setAuthor(currentUser);
        comment.setModifiedBy(currentUser.getId());

        // Handle parent comment if this is a reply
        if (request.getParentId() != null) {
            Comment parentComment = findCommentById(request.getParentId());
            
            // Verify parent comment belongs to the same lesson
            if (!parentComment.getLesson().getId().equals(lessonId)) {
                throw new ForbiddenException("Parent comment does not belong to this lesson");
            }
            
            comment.setParent(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);
        List<CommentVote> votes = commentVoteRepository.findAllVotesByLessonId(lessonId);
        return CommentMapper.toResponse(savedComment, currentUser, votes);
    }

    @Transactional
    public CommentResponse createCommentByLessonSlug(String lessonSlug, CommentRequest request) {
        User currentUser = getCurrentUser();
        Lesson lesson = lessonRepository.findBySlug(lessonSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with slug: " + lessonSlug));

        verifyLessonAccess(lesson, currentUser.getId());

        Comment comment = CommentMapper.toEntity(request);
        comment.setLesson(lesson);
        comment.setAuthor(currentUser);
        comment.setModifiedBy(currentUser.getId());

        if (request.getParentId() != null) {
            Comment parentComment = findCommentById(request.getParentId());
            if (!parentComment.getLesson().getId().equals(lesson.getId())) {
                throw new ForbiddenException("Parent comment does not belong to this lesson");
            }
            comment.setParent(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);
        List<CommentVote> votes = commentVoteRepository.findAllVotesByLessonId(lesson.getId());
        return CommentMapper.toResponse(savedComment, currentUser, votes);
    }


    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsForLesson(UUID lessonId, Pageable pageable) {
        User currentUser = getCurrentUser();
        Lesson lesson = findLessonById(lessonId);
        
        // Verify user has access to the lesson
        verifyLessonAccess(lesson, currentUser.getId());

        // Get all comments for the lesson (including replies)
        List<Comment> allComments = commentRepository.findAllCommentsByLessonId(lessonId);
        List<CommentVote> allVotes = commentVoteRepository.findAllVotesByLessonId(lessonId);
        
        // Apply pagination to all comments
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allComments.size());
        List<Comment> paginatedComments = allComments.subList(start, end);
        
        // Map to responses (parentId will indicate if it's a reply)
        List<CommentResponse> responses = paginatedComments.stream()
                .map(comment -> CommentMapper.toResponse(comment, currentUser, allVotes))
                .collect(Collectors.toList());
        
        // Create a Page object
        return new org.springframework.data.domain.PageImpl<>(
                responses, 
                pageable, 
                allComments.size()
        );
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsForLessonBySlug(String lessonSlug, Pageable pageable) {
        User currentUser = getCurrentUser();
        Lesson lesson = lessonRepository.findBySlug(lessonSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with slug: " + lessonSlug));

        verifyLessonAccess(lesson, currentUser.getId());

        // Get all comments for the lesson (including replies)
        List<Comment> allComments = commentRepository.findAllCommentsByLessonId(lesson.getId());
        List<CommentVote> allVotes = commentVoteRepository.findAllVotesByLessonId(lesson.getId());
        
        // Apply pagination to all comments
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allComments.size());
        List<Comment> paginatedComments = allComments.subList(start, end);
        
        // Map to responses (parentId will indicate if it's a reply)
        List<CommentResponse> responses = paginatedComments.stream()
                .map(comment -> CommentMapper.toResponse(comment, currentUser, allVotes))
                .collect(Collectors.toList());
        
        // Create a Page object
        return new org.springframework.data.domain.PageImpl<>(
                responses, 
                pageable, 
                allComments.size()
        );
    }

    @Transactional(readOnly = true)
    public CommentResponse getCommentById(UUID commentId) {
        User currentUser = getCurrentUser();
        Comment comment = findCommentById(commentId);
        
        // Verify user has access to the lesson
        verifyLessonAccess(comment.getLesson(), currentUser.getId());
        
        List<CommentVote> votes = commentVoteRepository.findAllVotesByLessonId(comment.getLesson().getId());
        return CommentMapper.toResponse(comment, currentUser, votes);
    }


    @Transactional
    public CommentResponse updateComment(UUID commentId, CommentRequest request) {
        User currentUser = getCurrentUser();
        Comment comment = findCommentById(commentId);
        
        // Verify ownership
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only update your own comments");
        }

        CommentMapper.updateEntityFromRequest(request, comment);
        comment.setModifiedBy(currentUser.getId());

        Comment updatedComment = commentRepository.save(comment);
        List<CommentVote> votes = commentVoteRepository.findAllVotesByLessonId(comment.getLesson().getId());
        return CommentMapper.toResponse(updatedComment, currentUser, votes);
    }

    @Transactional
    public void deleteComment(UUID commentId) {
        User currentUser = getCurrentUser();
        Comment comment = findCommentById(commentId);
        
        // Verify ownership or admin rights
        boolean canDelete = comment.getAuthor().getId().equals(currentUser.getId()) || isAdminUser(currentUser);
        if (!canDelete) {
            throw new ForbiddenException("You don't have permission to delete this comment");
        }

        // Soft delete - mark as deleted instead of actual deletion
        comment.setIsDeleted(true);
        comment.setModifiedBy(currentUser.getId());
        commentRepository.save(comment);
    }


    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Lesson findLessonById(UUID lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));
    }

    private Comment findCommentById(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
    }

    private void verifyLessonAccess(Lesson lesson, UUID userId) {
        // Check if user is enrolled in the course that contains this lesson
        boolean hasAccess = enrollmentRepository.existsByMemberIdAndCourseId(userId, lesson.getCourse().getId());
        if (!hasAccess) {
            throw new ForbiddenException("You must be enrolled in the course to access this lesson's comments");
        }
    }

    @Transactional
    public CommentResponse voteComment(UUID commentId, Boolean isUpvote) {
        User currentUser = getCurrentUser();
        Comment comment = findCommentById(commentId);
        
        // Verify user has access to the lesson
        verifyLessonAccess(comment.getLesson(), currentUser.getId());

        // Check if user already voted
        CommentVote existingVote = commentVoteRepository.findByCommentIdAndUserId(commentId, currentUser.getId())
                .orElse(null);

        if (existingVote != null) {
            if (existingVote.getVoteType().equals(isUpvote)) {
                // Remove vote if clicking the same vote type
                commentVoteRepository.delete(existingVote);
                // Decrease the corresponding vote count
                if (isUpvote) {
                    comment.setUpvotes(Math.max(0, comment.getUpvotes() - 1));
                } else {
                    comment.setDownvotes(Math.max(0, comment.getDownvotes() - 1));
                }
            } else {
                // Change vote type - need to adjust both counts
                Boolean oldVoteType = existingVote.getVoteType();
                existingVote.setVoteType(isUpvote);
                commentVoteRepository.save(existingVote);
                
                // Decrease old vote count and increase new vote count
                if (oldVoteType) {
                    // Was upvote, now downvote
                    comment.setUpvotes(Math.max(0, comment.getUpvotes() - 1));
                    comment.setDownvotes(comment.getDownvotes() + 1);
                } else {
                    // Was downvote, now upvote
                    comment.setDownvotes(Math.max(0, comment.getDownvotes() - 1));
                    comment.setUpvotes(comment.getUpvotes() + 1);
                }
            }
        } else {
            // Create new vote
            CommentVote newVote = CommentVote.builder()
                    .comment(comment)
                    .user(currentUser)
                    .voteType(isUpvote)
                    .build();
            commentVoteRepository.save(newVote);
            
            // Increase the corresponding vote count
            if (isUpvote) {
                comment.setUpvotes(comment.getUpvotes() + 1);
            } else {
                comment.setDownvotes(comment.getDownvotes() + 1);
            }
        }

        // Save the comment with updated vote counts
        Comment updatedComment = commentRepository.save(comment);
        
        List<CommentVote> votes = commentVoteRepository.findAllVotesByLessonId(comment.getLesson().getId());
        return CommentMapper.toResponse(updatedComment, currentUser, votes);
    }
    

    @Transactional(readOnly = true)
    public List<VoteResponse> getVotesByUserId(UUID userId) {
        User currentUser = getCurrentUser();
        
        // Check if user is requesting their own votes or is admin
        boolean canView = currentUser.getId().equals(userId) || isAdminUser(currentUser);
        if (!canView) {
            throw new ForbiddenException("You can only view your own votes");
        }
        
        List<CommentVote> votes = commentVoteRepository.findByUserId(userId);
        return votes.stream()
                .map(this::mapToVoteResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VoteResponse> getVotesByUserIdAndLessonId(UUID userId, UUID lessonId) {
        User currentUser = getCurrentUser();
        
        // Check if user is requesting their own votes or is admin
        boolean canView = currentUser.getId().equals(userId) || isAdminUser(currentUser);
        if (!canView) {
            throw new ForbiddenException("You can only view your own votes");
        }
        
        // Verify user has access to the lesson
        Lesson lesson = findLessonById(lessonId);
        verifyLessonAccess(lesson, currentUser.getId());
        
        List<CommentVote> votes = commentVoteRepository.findByUserIdAndLessonId(userId, lessonId);
        return votes.stream()
                .map(this::mapToVoteResponse)
                .collect(Collectors.toList());
    }

    private VoteResponse mapToVoteResponse(CommentVote vote) {
        return VoteResponse.builder()
                .id(vote.getId())
                .commentId(vote.getComment().getId())
                .commentContent(vote.getComment().getContent())
                .lessonId(vote.getComment().getLesson().getId())
                .lessonTitle(vote.getComment().getLesson().getTitle())
                .userId(vote.getUser().getId())
                .userName(vote.getUser().getFullName())
                .isUpvote(vote.getVoteType())
                .creation(vote.getCreation())
                .build();
    }

    private boolean isAdminUser(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> {
                    String name = role.getRole().name();
                    return "SYSTEM_MANAGER".equals(name) || "MODERATOR".equals(name);
                });
    }
}
