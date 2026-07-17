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
@Table(name = "resident_contacts")
public class ResidentContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    @Column(name = "relationship_type", nullable = false, length = 50)
    private String relationshipType;

    @Column(name = "is_guarantor", nullable = false)
    @Builder.Default
    private Boolean isGuarantor = false;

    @Column(name = "is_emergency_contact", nullable = false)
    @Builder.Default
    private Boolean isEmergencyContact = false;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "financial_responsibility_pct", precision = 5, scale = 2)
    private BigDecimal financialResponsibilityPct;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}

