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
public class RegenerateSchedulesResponse {
    private Long orderId;
    private List<ScheduleInfo> schedules;

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