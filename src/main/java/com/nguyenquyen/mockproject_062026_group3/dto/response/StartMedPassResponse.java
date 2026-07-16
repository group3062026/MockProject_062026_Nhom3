package com.nguyenquyen.mockproject_062026_group3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StartMedPassResponse {
    private String sessionId;
    private String expiresAt;
    private ResidentInfo resident;
    private List<PendingMedication> pendingMedications;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResidentInfo {
        private Long id;
        private String fullName;
        private String roomNumber;
        private String bedNumber;
        private String dateOfBirth;
        private List<String> allergies;
        private Boolean allergyConfirmed;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PendingMedication {
        private Long orderId;
        private String drugName;
        private String dosage;
        private String route;
        private String frequency;
        private String scheduledTime;
        private Boolean isControlledSubstance;
    }
}