package com.nguyenquyen.mockproject_062026_group3.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResidentSensitiveInfoResponse {
    private String ssnMasked;
    private String medicalRecordNumberMasked;
    private String bankAccount; // Masked value or plaintext
    private String primaryInsuranceId;

    // Plaintext fields for revealed response
    private String ssn;
    private String medicalRecordNumber;
}
