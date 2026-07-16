package com.nguyenquyen.mockproject_062026_group3.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchAdministerRequest {
    private String sessionId;
    private Long residentId;
    private List<Long> orderIds;
    private List<Long> scheduleIds;
    private Long witnessedBy;
    private String notes;
}