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
public class MarScheduleShiftResponse {
    private String shift;
    private String date;
    private List<ShiftScheduleItem> schedule;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShiftScheduleItem {
        private Long residentId;
        private String residentName;
        private String roomNumber;
        private List<ShiftMedication> medications;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShiftMedication {
        private Long orderId;
        private Long scheduleId;
        private String drugName;
        private String dosage;
        private String scheduledTime;
        private Boolean isActive;
        private String status;
    }
}