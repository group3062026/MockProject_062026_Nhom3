package com.mockproject.group3.dto.admin.carelevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for updating a care level rate.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCareLevelRateRequest {
    private Long careLevelId;
    private BigDecimal dailyRate;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}
