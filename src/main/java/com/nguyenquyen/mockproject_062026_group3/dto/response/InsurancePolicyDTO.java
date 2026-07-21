package com.nguyenquyen.mockproject_062026_group3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO cho chính sách bảo hiểm của cư dân
 * sc-035
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InsurancePolicyDTO {
    
    private Long id;
    private String providerName;
    private String providerType;  // MEDICARE, MEDICAID, PRIVATE, OTHER
    private String policyNumber;
    private String groupNumber;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean isPrimary;  // Bảo hiểm chính?
}

