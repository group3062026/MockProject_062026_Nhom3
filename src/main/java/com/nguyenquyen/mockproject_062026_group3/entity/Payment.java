package com.nguyenquyen.mockproject_062026_group3.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "payer_type", nullable = false, length = 20)
    private String payerType; // MEDICARE, MEDICAID, PRIVATE_INSURANCE, FAMILY

    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod; // CREDIT_CARD, ACH, CHECK, CASH, INSURANCE_DIRECT

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_token_encrypted", length = 512)
    private String paymentTokenEncrypted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by", nullable = false)
    private User receivedBy;

    @Column(name = "paid_at", nullable = false)
    @Builder.Default
    private OffsetDateTime paidAt = OffsetDateTime.now();
}

