package com.mockproject.group3.dto.admin.carelevel;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating a new care level rate.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCareLevelRateRequest {

    @NotNull(message = "Care level ID is required")
    private Long careLevelId;

    @NotNull(message = "Daily rate is required")
    private BigDecimal dailyRate;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}
