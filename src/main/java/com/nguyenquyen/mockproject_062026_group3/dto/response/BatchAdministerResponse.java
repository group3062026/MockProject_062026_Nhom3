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
public class BatchAdministerResponse {
    private Integer total;
    private Integer administered;
    private Integer failed;
    private List<BatchLog> logs;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BatchLog {
        private Long orderId;
        private Long scheduleId;
        private String status;
        private Long logId;
        private String errorMessage;
    }
}