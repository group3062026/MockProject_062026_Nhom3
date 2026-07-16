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
public class MarDashboardResponse {
    private String shift;
    private String date;
    private DashboardSummary summary;
    private List<AllergyAlert> globalAllergyAlerts;
    private List<MedPassItem> medPassList;
    private String shiftHandoffNotes;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DashboardSummary {
        private int pending;
        private int completed;
        private int overdue;
        private int held;
        private int notAvailable;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AllergyAlert {
        private Long residentId;
        private String residentName;
        private String allergy;
        private Boolean unconfirmed;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MedPassItem {
        private Long residentId;
        private String residentName;
        private String roomNumber;
        private String bedNumber;
        private String status;
        private NextMedication nextMedication;
        private Boolean hasUnconfirmedAllergy;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NextMedication {
        private Long orderId;
        private String drugName;
        private String scheduledTime;
        private String timeWindowStart;
        private String timeWindowEnd;
    }
}