package com.nguyenquyen.mockproject_062026_group3.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @Column(name = "billing_period_start", nullable = false)
    private LocalDate billingPeriodStart;

    @Column(name = "billing_period_end", nullable = false)
    private LocalDate billingPeriodEnd;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "medicare_covered_amount", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal medicareCoveredAmount = BigDecimal.ZERO;

    @Column(name = "medicaid_covered_amount", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal medicaidCoveredAmount = BigDecimal.ZERO;

    @Column(name = "private_insurance_covered_amount", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal privateInsuranceCoveredAmount = BigDecimal.ZERO;

    @Column(name = "patient_responsibility_amount", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal patientResponsibilityAmount = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // DRAFT, SENT, PARTIALLY_PAID, PAID, OVERDUE, VOID

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InvoiceLineItem> lineItems;
}

