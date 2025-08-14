package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "lms_settings")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LmsSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "default_home")
    private String defaultHome;

    @Column(name = "send_calendar_invite_for_evaluations", columnDefinition = "boolean default false")
    private Boolean sendCalendarInviteForEvaluations;

    @Column(name = "persona_captured", columnDefinition = "boolean default false")
    private Boolean personaCaptured;

    @Column(name = "allow_guest_access", columnDefinition = "boolean default false")
    private Boolean allowGuestAccess;

    @Column(name = "enable_learning_paths", columnDefinition = "boolean default false")
    private Boolean enableLearningPaths;

    @Column(name = "prevent_skipping_videos", columnDefinition = "boolean default false")
    private Boolean preventSkippingVideos;

    @Column(name = "unsplash_access_key")
    private String unsplashAccessKey;

    @Column(name = "livecode_url")
    private String livecodeUrl;

    @Column(name = "show_day_view", columnDefinition = "boolean default false")
    private Boolean showDayView;

    @Column(name = "show_dashboard", columnDefinition = "boolean default true")
    private Boolean showDashboard;

    @Column(name = "show_courses", columnDefinition = "boolean default true")
    private Boolean showCourses;

    @Column(name = "show_students", columnDefinition = "boolean default true")
    private Boolean showStudents;

    @Column(name = "show_assessments", columnDefinition = "boolean default true")
    private Boolean showAssessments;

    @Column(name = "show_live_class", columnDefinition = "boolean default true")
    private Boolean showLiveClass;

    @Column(name = "show_discussions", columnDefinition = "boolean default true")
    private Boolean showDiscussions;

    @Column(name = "show_emails", columnDefinition = "boolean default true")
    private Boolean showEmails;

    @Column(name = "user_category")
    private String userCategory;

    @Column(name = "disable_signup", columnDefinition = "boolean default false")
    private Boolean disableSignup;

    @Column(name = "custom_signup_content", columnDefinition = "TEXT")
    private String customSignupContent;

    @Column(name = "payment_gateway")
    private String paymentGateway;

    @Column(name = "default_currency", length = 10)
    private String defaultCurrency;

    @Column(name = "exception_country")
    private String exceptionCountry;

    @Column(name = "apply_gst", columnDefinition = "boolean default false")
    private Boolean applyGst;

    @Column(name = "show_usd_equivalent", columnDefinition = "boolean default false")
    private Boolean showUsdEquivalent;

    @Column(name = "apply_rounding", columnDefinition = "boolean default false")
    private Boolean applyRounding;

    @Column(name = "no_payments_app", columnDefinition = "boolean default false")
    private Boolean noPaymentsApp;

    @Column(name = "certification_template")
    private String certificationTemplate;

    @Column(name = "batch_confirmation_template")
    private String batchConfirmationTemplate;

    @Column(name = "payment_reminder_template")
    private String paymentReminderTemplate;

    @Column(name = "meta_description", columnDefinition = "TEXT")
    private String metaDescription;

    @Column(name = "meta_image")
    private String metaImage;

    @Column(name = "meta_keywords", columnDefinition = "TEXT")
    private String metaKeywords;

    @CreationTimestamp
    @Column(name = "creation", nullable = false, updatable = false)
    private OffsetDateTime creation;

    @UpdateTimestamp
    @Column(name = "modified", nullable = false)
    private OffsetDateTime modified;

    @Column(name = "modified_by")
    private UUID modifiedBy;
} 