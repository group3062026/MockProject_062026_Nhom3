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
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shift_assignments")
public class ShiftAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // SCHEDULED, CONFIRMED, CALLED_OUT, COMPLETED

    @Column(name = "clock_in_at")
    private OffsetDateTime clockInAt;

    @Column(name = "clock_out_at")
    private OffsetDateTime clockOutAt;

    @Column(name = "is_holiday_shift", nullable = false)
    @Builder.Default
    private Boolean isHolidayShift = false;

    @Column(name = "pay_multiplier", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal payMultiplier = new BigDecimal("1.00");
}

