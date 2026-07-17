package com.nguyenquyen.mockproject_062026_group3.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicationAuditRequest {
    private Long residentId;
    private Long orderId;
    private String action; // INSERT, UPDATE, DELETE
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer page;
    private Integer limit;
}