package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @Column(name = "billing_name", nullable = false)
    private String billingName;

    private String source;

    @Column(name = "payment_for_document_type")
    private String paymentForDocumentType;

    @Column(name = "payment_for_document")
    private String paymentForDocument;

    @Column(name = "payment_received")
    private Boolean paymentReceived = false;

    @Column(name = "payment_for_certificate")
    private Boolean paymentForCertificate = false;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "amount_with_gst")
    private BigDecimal amountWithGst;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "address_id")
    private String addressId;

    private String gstin;
    private String pan;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime creation;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime modified;
}
