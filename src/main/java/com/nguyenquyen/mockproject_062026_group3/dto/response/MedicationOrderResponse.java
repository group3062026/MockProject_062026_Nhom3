package com.nguyenquyen.mockproject_062026_group3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicationOrderResponse {
    private Long id;
    private ResidentInfo resident;
    private String drugName;
    private String dosage;
    private String route;
    private String frequency;
    private Boolean isControlledSubstance;
    private String status;
    private PrescriberInfo prescribedBy;
    private String lastAdministeredAt;
    private String lastAdministeredBy;
    private Boolean allergyConflict;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResidentInfo {
        private Long id;
        private String displayName;
        private String roomNumber;
        private String bedNumber;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrescriberInfo {
        private Long id;
        private String displayName;
        private String licenseNumber;
    }
}