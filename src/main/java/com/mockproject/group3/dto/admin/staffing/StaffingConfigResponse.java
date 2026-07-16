package com.mockproject.group3.dto.admin.staffing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for staffing ratio configuration (AD-07).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StaffingConfigResponse {
    private Long id;
    private Long facilityId;
    private BigDecimal minHrsPerResidentDay;
    private Integer warnBelowPercentage;
}
