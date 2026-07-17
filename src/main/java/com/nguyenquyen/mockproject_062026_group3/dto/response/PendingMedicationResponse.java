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
public class PendingMedicationResponse {
    private Long residentId;
    private List<PendingMedication> pendingMedications;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PendingMedication {
        private Long orderId;
        private Long scheduleId;
        private String drugName;
        private String scheduledTime;
        private String timeWindowStart;
        private String timeWindowEnd;
        private Boolean isOverdue;
        private Integer minutesUntilDue;
        private Boolean requiresWitness;
    }
}