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
public class MarExportResponse {
    private List<MarExportRow> data;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MarExportRow {
        private String date;
        private String residentName;
        private String medication;
        private String status;
        private String administeredBy;
        private String witnessedBy;
        private String overrideReason;
        private String timestamp;
    }
}