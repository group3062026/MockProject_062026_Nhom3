package com.nguyenquyen.mockproject_062026_group3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefuseMedicationResponse {
    private Long logId;
    private Long orderId;
    private Long scheduleId;
    private String status;
    private String overrideReason;
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