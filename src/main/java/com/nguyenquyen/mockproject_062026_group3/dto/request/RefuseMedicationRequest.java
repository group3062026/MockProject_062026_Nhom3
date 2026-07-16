package com.nguyenquyen.mockproject_062026_group3.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefuseMedicationRequest {
    private String sessionId;
    private Long orderId;
    private Long scheduleId;
    private String overrideReason;
}