package com.nguyenquyen.mockproject_062026_group3.dto;

import com.nguyenquyen.mockproject_062026_group3.entity.CareLevelRate;
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
public class CareLevelRateResponse {
    private Long id;
    private Long careLevelId;
    private Long facilityId;
    private BigDecimal dailyRate;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    public static CareLevelRateResponse fromEntity(CareLevelRate entity) {
        if (entity == null) return null;
        return CareLevelRateResponse.builder()
                .id(entity.getId())
                .careLevelId(entity.getCareLevel() != null ? entity.getCareLevel().getId() : null)
                .facilityId(entity.getFacility() != null ? entity.getFacility().getId() : null)
                .dailyRate(entity.getDailyRate())
                .effectiveFrom(entity.getEffectiveFrom())
                .effectiveTo(entity.getEffectiveTo())
                .build();
    }
}
