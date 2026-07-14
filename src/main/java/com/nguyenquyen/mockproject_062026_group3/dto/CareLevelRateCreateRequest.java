package com.nguyenquyen.mockproject_062026_group3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CareLevelRateCreateRequest {
    private Long careLevelId;
    private Long facilityId;
    private BigDecimal dailyRate;
    private LocalDate effectiveFrom;
}
