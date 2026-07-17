package com.nguyenquyen.mockproject_062026_group3.dto.admin.facility;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for facility settings (AD-05).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FacilityResponse {
    private Long id;
    private String facilityCode;
    private String name;
    private String licenseNumber;
    private String targetState;
    private AddressDto address;
    private String phoneNumber;
}
