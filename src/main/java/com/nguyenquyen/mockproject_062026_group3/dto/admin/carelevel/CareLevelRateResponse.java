package com.nguyenquyen.mockproject_062026_group3.dto.admin.carelevel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for care level rate (LOC rate).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CareLevelRateResponse {
    private Long id;
    private Long careLevelId;
    private Long facilityId;
    private BigDecimal dailyRate;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}
