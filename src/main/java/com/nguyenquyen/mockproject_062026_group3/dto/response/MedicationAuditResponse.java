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
public class MedicationAuditResponse {
    private Integer total;
    private Integer page;
    private Integer limit;
    private List<AuditLog> logs;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuditLog {
        private Long id;
        private String tableName;
        private String recordId;
        private String action;
        private String oldData;
        private String newData;
        private PerformedBy performedBy;
        private String performedAt;
        private String ipAddress;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class PerformedBy {
            private Long id;
            private String displayName;
        }
    }
}