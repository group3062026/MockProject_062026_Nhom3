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
public class MarResidentResponse {
    private ResidentInfo resident;
    private DateRange dateRange;
    private List<MedicationGrid> medicationGrid;
    private SummaryStats summaryStats;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResidentInfo {
        private Long id;
        private String fullName;
        private String roomNumber;
        private String bedNumber;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DateRange {
        private String start;
        private String end;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MedicationGrid {
        private Long orderId;
        private String drugName;
        private String dosage;
        private String route;
        private String frequency;
        private List<DayDetail> days;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DayDetail {
        private String date;
        private Long scheduleId;
        private String scheduledTime;
        private String status;
        private AdminBy administeredBy;
        private AdminBy witnessedBy;
        private String loggedAt;
        private String overrideReason;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminBy {
        private Long id;
        private String displayName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SummaryStats {
        private int totalScheduled;
        private int administered;
        private int held;
        private int refused;
        private int notAvailable;
        private int overrides;
    }
}