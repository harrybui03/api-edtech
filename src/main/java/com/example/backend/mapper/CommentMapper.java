package com.example.backend.mapper;

import com.example.backend.dto.model.CommentDto;
import com.example.backend.dto.request.comment.CommentRequest;
import com.example.backend.dto.response.comment.CommentResponse;
import com.example.backend.entity.Comment;
import com.example.backend.entity.CommentVote;
import com.example.backend.entity.User;

import java.util.Collections;
import java.util.List;

public final class CommentMapper {

    public static CommentDto toDto(Comment comment, User currentUser, List<CommentVote> votes) {
        if (comment == null) {
            return null;
        }
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setLessonId(comment.getLesson() != null ? comment.getLesson().getId() : null);
        dto.setAuthorId(comment.getAuthor() != null ? comment.getAuthor().getId() : null);
        dto.setAuthorName(comment.getAuthor() != null ? comment.getAuthor().getFullName() : null);
        dto.setAuthorImage(comment.getAuthor() != null ? comment.getAuthor().getUserImage() : null);
        dto.setContent(comment.getContent());
        dto.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
        dto.setUpvotes(comment.getUpvotes());
        dto.setDownvotes(comment.getDownvotes());
        dto.setIsDeleted(comment.getIsDeleted());
        dto.setCreation(comment.getCreation());
        dto.setModified(comment.getModified());
        
        // Set permissions and user vote
        if (currentUser != null) {
            dto.setCanEdit(comment.getAuthor().getId().equals(currentUser.getId()));
            dto.setCanDelete(comment.getAuthor().getId().equals(currentUser.getId()) || isAdminUser(currentUser));
            
            // Set user vote
            CommentVote userVote = votes.stream()
                .filter(vote -> vote.getUser().getId().equals(currentUser.getId()) && 
                               vote.getComment().getId().equals(comment.getId()))
                .findFirst()
                .orElse(null);
            
            if (userVote != null) {
                dto.setUserVote(userVote.getVoteType() ? "upvote" : "downvote");
            }
        }
        
        // No replies field - use parentId to identify replies
        dto.setReplies(Collections.emptyList());
        
        return dto;
    }

    public static CommentResponse toResponse(Comment comment, User currentUser, List<CommentVote> votes) {
        if (comment == null) {
            return null;
        }
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setLessonId(comment.getLesson() != null ? comment.getLesson().getId() : null);
        response.setLessonTitle(comment.getLesson() != null ? comment.getLesson().getTitle() : null);
        response.setAuthorId(comment.getAuthor() != null ? comment.getAuthor().getId() : null);
        response.setAuthorName(comment.getAuthor() != null ? comment.getAuthor().getFullName() : null);
        response.setAuthorImage(comment.getAuthor() != null ? comment.getAuthor().getUserImage() : null);
        response.setContent(comment.getContent());
        response.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
        response.setUpvotes(comment.getUpvotes());
        response.setDownvotes(comment.getDownvotes());
        response.setCreation(comment.getCreation());
        response.setModified(comment.getModified());
        
        // Set permissions and user vote
        if (currentUser != null) {
            response.setCanEdit(comment.getAuthor().getId().equals(currentUser.getId()));
            response.setCanDelete(comment.getAuthor().getId().equals(currentUser.getId()) || isAdminUser(currentUser));
            
            // Set user vote
            CommentVote userVote = votes.stream()
                .filter(vote -> vote.getUser().getId().equals(currentUser.getId()) && 
                               vote.getComment().getId().equals(comment.getId()))
                .findFirst()
                .orElse(null);
            
            if (userVote != null) {
                response.setUserVote(userVote.getVoteType() ? "upvote" : "downvote");
            }
        }
        
        // No replies field - use parentId to identify replies
        response.setReplies(Collections.emptyList());
        
        return response;
    }

    public static Comment toEntity(CommentRequest request) {
        if (request == null) {
            return null;
        }
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setUpvotes(0);
        comment.setDownvotes(0);
        comment.setIsDeleted(false);
        return comment;
    }

    public static void updateEntityFromRequest(CommentRequest request, Comment comment) {
        if (request == null || comment == null) {
            return;
        }
        comment.setContent(request.getContent());
    }


    private static boolean isAdminUser(User user) {
        // You can implement this based on your user role system
        return user.getRoles().stream()
            .anyMatch(role -> "ADMIN".equals(role.getRole().name()));
    }
}
