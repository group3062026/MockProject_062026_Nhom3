package com.mockproject.group3.dto.admin.facility;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating facility settings.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFacilityRequest {

    @NotBlank(message = "Facility code is required")
    private String facilityCode;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @NotBlank(message = "Target state is required")
    private String targetState;

    private AddressDto address;

    private String phoneNumber;
}
