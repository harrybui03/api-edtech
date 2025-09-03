package com.example.backend.entity;

import com.example.backend.constant.UserRoleEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "role"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "user_role_enum")
    private UserRoleEnum role;

    @CreationTimestamp
    @Column(name = "creation", nullable = false, updatable = false)
    private OffsetDateTime creation;
}
