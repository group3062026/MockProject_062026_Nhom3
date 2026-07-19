package com.nguyenquyen.mockproject_062026_group3.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OverrideVerificationRequest {
    private String sessionId;
    private Long orderId;
    private Long scheduleId;
    private String overrideReason;
    private String otherReasonText;
    private Boolean confirmClinicallyJustified;
    private Long witnessedBy;
    private String notes;
}