package com.nguyenquyen.mockproject_062026_group3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * sc-035
 * DTO cho ước tính chi phí
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CostEstimateDTO {
    
    private Long residentId;
    private String residentName;
    private Integer estimatedDays;
    
    // Chi phí hàng ngày
    private BigDecimal dailyRate;
    
    // Tính toán
    private BigDecimal estimatedTotalCost;
    private BigDecimal estimatedInsuranceCovered;
    private BigDecimal estimatedPatientResponsibility;
    
    // Chi tiết
    private String careLevelName;
    private String facilityName;
    
    // Ghi chú
    private String notes;  // "This is an estimate based on current care level..."
}

