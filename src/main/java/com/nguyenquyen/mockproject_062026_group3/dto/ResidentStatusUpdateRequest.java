package com.nguyenquyen.mockproject_062026_group3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResidentStatusUpdateRequest {
    private String status; // PENDING, ACTIVE, DISCHARGED, DECEASED
    private String reason;
    private LocalDate effectiveDate;
}
