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

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "medication_logs")
public class MedicationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // ADMINISTERED, REFUSED, HELD, NOT_AVAILABLE

    @Column(name = "is_clinically_justified", nullable = false)
    @Builder.Default
    private Boolean isClinicallyJustified = false;

    @Column(name = "override_reason", length = 500)
    private String overrideReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private MedicationOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administered_by", nullable = false)
    private User administeredBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "witnessed_by")
    private User witnessedBy;

    @Column(name = "logged_at", nullable = false)
    @Builder.Default
    private OffsetDateTime loggedAt = OffsetDateTime.now();
}

