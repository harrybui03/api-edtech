package com.example.backend.service;

import com.example.backend.constant.VoteType;
import com.example.backend.dto.model.CommentDto;
import com.example.backend.dto.request.comment.CommentRequest;
import com.example.backend.dto.response.comment.CommentResponse;
import com.example.backend.entity.*;
import com.example.backend.excecption.ForbiddenException;
import com.example.backend.excecption.InvalidRequestDataException;
import com.example.backend.excecption.ResourceNotFoundException;
import com.example.backend.mapper.CommentMapper;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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

        Comment savedComment = commentRepository.save(comment);
        List<CommentVote> votes = commentVoteRepository.findAllVotesByLessonId(lessonId);
        return CommentMapper.toResponse(savedComment, currentUser, votes);
    }

    @Transactional
    public CommentResponse createReply(UUID commentId, CommentRequest request) {
        User currentUser = getCurrentUser();
        Comment parentComment = findCommentById(commentId);
        
        // Verify user has access to the lesson
        verifyLessonAccess(parentComment.getLesson(), currentUser.getId());

        Comment reply = CommentMapper.toEntity(request);
        reply.setLesson(parentComment.getLesson());
        reply.setParent(parentComment);
        reply.setAuthor(currentUser);
        reply.setModifiedBy(currentUser.getId());

        Comment savedReply = commentRepository.save(reply);
        List<CommentVote> votes = commentVoteRepository.findAllVotesByLessonId(parentComment.getLesson().getId());
        return CommentMapper.toResponse(savedReply, currentUser, votes);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsForLesson(UUID lessonId, Pageable pageable) {
        User currentUser = getCurrentUser();
        Lesson lesson = findLessonById(lessonId);
        
        // Verify user has access to the lesson
        verifyLessonAccess(lesson, currentUser.getId());

        Page<Comment> comments = commentRepository.findRootCommentsByLessonId(lessonId, pageable);
        List<CommentVote> allVotes = commentVoteRepository.findAllVotesByLessonId(lessonId);
        
        return comments.map(comment -> CommentMapper.toResponse(comment, currentUser, allVotes));
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

    @Transactional
    public CommentResponse upvoteComment(UUID commentId) {
        return voteComment(commentId, VoteType.UPVOTE);
    }

    @Transactional
    public CommentResponse downvoteComment(UUID commentId) {
        return voteComment(commentId, VoteType.DOWNVOTE);
    }

    @Transactional
    private CommentResponse voteComment(UUID commentId, VoteType voteType) {
        User currentUser = getCurrentUser();
        Comment comment = findCommentById(commentId);
        
        // Verify user has access to the lesson
        verifyLessonAccess(comment.getLesson(), currentUser.getId());

        // Check if user already voted
        CommentVote existingVote = commentVoteRepository.findByCommentIdAndUserId(commentId, currentUser.getId())
                .orElse(null);

        if (existingVote != null) {
            if (existingVote.getVoteType() == voteType) {
                // Remove vote if clicking the same vote type
                commentVoteRepository.delete(existingVote);
                updateCommentVoteCounts(comment);
            } else {
                // Change vote type
                existingVote.setVoteType(voteType);
                commentVoteRepository.save(existingVote);
                updateCommentVoteCounts(comment);
            }
        } else {
            // Create new vote
            CommentVote newVote = CommentVote.builder()
                    .comment(comment)
                    .user(currentUser)
                    .voteType(voteType)
                    .build();
            commentVoteRepository.save(newVote);
            updateCommentVoteCounts(comment);
        }

        Comment updatedComment = commentRepository.save(comment);
        List<CommentVote> votes = commentVoteRepository.findAllVotesByLessonId(comment.getLesson().getId());
        return CommentMapper.toResponse(updatedComment, currentUser, votes);
    }

    private void updateCommentVoteCounts(Comment comment) {
        long upvotes = commentVoteRepository.countByCommentIdAndVoteType(comment.getId(), VoteType.UPVOTE);
        long downvotes = commentVoteRepository.countByCommentIdAndVoteType(comment.getId(), VoteType.DOWNVOTE);
        
        comment.setUpvotes((int) upvotes);
        comment.setDownvotes((int) downvotes);
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

    private boolean isAdminUser(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getRole().name()));
    }
}
