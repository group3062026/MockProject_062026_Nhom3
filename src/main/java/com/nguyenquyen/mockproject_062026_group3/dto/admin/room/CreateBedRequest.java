package com.nguyenquyen.mockproject_062026_group3.dto.admin.room;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new bed.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateBedRequest {

    @NotBlank(message = "Bed number is required")
    private String bedNumber;

    private String status;
}
