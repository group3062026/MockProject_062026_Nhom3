package com.nguyenquyen.mockproject_062026_group3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiscontinueMedicationOrderResponse {
    private Long id;
    private String status;
    private OffsetDateTime discontinuedAt;
    private DiscontinuedBy discontinuedBy;
    private String discontinueReason;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DiscontinuedBy {
        private Long id;
        private String displayName;
    }
}