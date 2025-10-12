package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "comment_votes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentVote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "vote_type", nullable = false)
    private Boolean voteType; // true = UPVOTE, false = DOWNVOTE

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime creation;

    // Unique constraint: one vote per user per comment
    @Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"comment_id", "user_id"})
    })
    public static class UniqueVoteConstraint {}
}
