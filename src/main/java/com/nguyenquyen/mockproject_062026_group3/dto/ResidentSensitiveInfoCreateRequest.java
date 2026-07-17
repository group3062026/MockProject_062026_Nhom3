package com.nguyenquyen.mockproject_062026_group3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResidentSensitiveInfoCreateRequest {
    private String ssn;
    private String medicalRecordNumber;
    private String primaryInsuranceId;
    private String bankAccount;
}
