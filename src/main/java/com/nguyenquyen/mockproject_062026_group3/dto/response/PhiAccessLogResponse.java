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
public class PhiAccessLogResponse {
    private List<PhiAccessLog> logs;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PhiAccessLog {
        private Long id;
        private String tableName;
        private String recordId;
        private AccessedBy accessedBy;
        private String accessType;
        private String accessReason;
        private String ipAddress;
        private String accessedAt;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class AccessedBy {
            private Long id;
            private String displayName;
        }
    }
}