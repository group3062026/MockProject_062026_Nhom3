package com.nguyenquyen.mockproject_062026_group3.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResidentContactUpdateRequest {
    private String relationshipType;
    private Boolean isGuarantor;
    private Boolean isEmergencyContact;
    private Boolean isPrimary;
    private BigDecimal financialResponsibilityPct;
}
