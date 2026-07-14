package com.nguyenquyen.mockproject_062026_group3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CareLevelRateCreateResponse {
    private CareLevelRateResponse newRate;
    private PreviousRateClosed previousRateClosed;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PreviousRateClosed {
        private Long id;
        private LocalDate effectiveTo;
    }
}
