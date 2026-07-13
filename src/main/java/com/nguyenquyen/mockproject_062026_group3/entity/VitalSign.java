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
@Table(name = "vital_signs")
public class VitalSign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by", nullable = false)
    private User recordedBy;

    @Column(name = "blood_pressure_systolic")
    private Short bloodPressureSystolic;

    @Column(name = "blood_pressure_diastolic")
    private Short bloodPressureDiastolic;

    @Column(name = "heart_rate_bpm")
    private Short heartRateBpm;

    @Column(name = "respiratory_rate")
    private Short respiratoryRate;

    @Column(name = "temperature_fahrenheit", precision = 4, scale = 1)
    private BigDecimal temperatureFahrenheit;

    @Column(name = "spo2_percentage")
    private Short spo2Percentage;

    @Column(name = "pain_scale")
    private Short painScale;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "recorded_at", nullable = false)
    @Builder.Default
    private OffsetDateTime recordedAt = OffsetDateTime.now();
}
