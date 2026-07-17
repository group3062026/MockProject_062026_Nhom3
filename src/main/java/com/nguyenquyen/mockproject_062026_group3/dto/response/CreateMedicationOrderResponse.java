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
public class CreateMedicationOrderResponse {
    private Long id;
    private Long residentId;
    private String drugName;
    private String dosage;
    private String route;
    private String frequency;
    private Boolean isControlledSubstance;
    private String status;
    private Long prescribedBy;
    private List<ScheduleInfo> schedules;
    private OffsetDateTime createdAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScheduleInfo {
        private Long id;
        private String scheduledTime;
        private Boolean isActive;
    }
}