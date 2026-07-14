package com.mockproject.group3.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "public_holidays")
public class PublicHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "holiday_name", nullable = false, length = 150)
    private String holidayName;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "holiday_type", nullable = false, length = 30)
    private String holidayType; // FEDERAL, STATE

    @Column(name = "state_code", length = 10)
    private String stateCode; // CA, HI, etc. Null if FEDERAL

    @Column(name = "pay_multiplier", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal payMultiplier = new BigDecimal("1.50");

    @Column(name = "is_observed", nullable = false)
    @Builder.Default
    private Boolean isObserved = true;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}

