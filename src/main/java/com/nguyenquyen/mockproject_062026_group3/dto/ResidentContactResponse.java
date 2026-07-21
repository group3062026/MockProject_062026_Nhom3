package com.nguyenquyen.mockproject_062026_group3.dto;

import com.nguyenquyen.mockproject_062026_group3.entity.ResidentContact;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResidentContactResponse {
    private Long id;
    private Long contactId;
    private String relationshipType;
    private Boolean isGuarantor;
    private Boolean isEmergencyContact;
    private Boolean isPrimary;
    private BigDecimal financialResponsibilityPct;

    public static ResidentContactResponse fromEntity(ResidentContact entity) {
        if (entity == null) return null;
        return ResidentContactResponse.builder()
                .id(entity.getId())
                .contactId(entity.getContact().getId())
                .relationshipType(entity.getRelationshipType())
                .isGuarantor(entity.getIsGuarantor())
                .isEmergencyContact(entity.getIsEmergencyContact())
                .isPrimary(entity.getIsPrimary())
                .financialResponsibilityPct(entity.getFinancialResponsibilityPct())
                .build();
    }
}
