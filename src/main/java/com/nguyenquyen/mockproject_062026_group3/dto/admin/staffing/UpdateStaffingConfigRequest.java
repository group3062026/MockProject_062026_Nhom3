package com.nguyenquyen.mockproject_062026_group3.dto.admin.staffing;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating staffing ratio configuration.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStaffingConfigRequest {

    @NotNull(message = "Min hours per resident day is required")
    private BigDecimal minHrsPerResidentDay;

    @NotNull(message = "Warn below percentage is required")
    private Integer warnBelowPercentage;
}
