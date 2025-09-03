package com.example.backend.entity;

import com.example.backend.constant.UserType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String username;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "user_image")
    private String userImage;

    @Column(columnDefinition = "boolean default true")
    private Boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", columnDefinition = "user_type_enum default 'WEBSITE_USER'")
    private UserType userType;

    @Column(name = "last_active")
    private OffsetDateTime lastActive;

    @CreationTimestamp
    @Column(name = "creation", nullable = false, updatable = false)
    private OffsetDateTime creation;

    @UpdateTimestamp
    @Column(name = "modified", nullable = false)
    private OffsetDateTime modified;

    @Column(name = "modified_by")
    private UUID modifiedBy;

    @OneToMany(mappedBy = "user" , cascade = CascadeType.ALL ,orphanRemoval = true, fetch =  FetchType.EAGER)
    private Set<UserRole> roles = new HashSet<>();

}
