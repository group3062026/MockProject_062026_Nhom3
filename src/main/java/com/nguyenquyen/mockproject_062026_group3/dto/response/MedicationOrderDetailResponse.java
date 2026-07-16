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
public class MedicationOrderDetailResponse {
    private Long id;
    private ResidentInfo resident;
    private String drugName;
    private String dosage;
    private String route;
    private String frequency;
    private Boolean isControlledSubstance;
    private String status;
    private PrescriberInfo prescribedBy;
    private String prescriberNotes;
    private Boolean allergyConflict;
    private List<ScheduleInfo> schedules;
    private List<RecentLog> recentLogs;
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

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScheduleInfo {
        private Long id;
        private String scheduledTime;
        private Boolean isActive;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentLog {
        private Long logId;
        private Long scheduleId;
        private String status;
        private AdminBy administeredBy;
        private String loggedAt;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class AdminBy {
            private Long id;
            private String displayName;
        }
    }
}