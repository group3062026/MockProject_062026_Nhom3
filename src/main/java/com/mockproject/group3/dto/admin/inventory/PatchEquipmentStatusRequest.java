package com.mockproject.group3.dto.admin.inventory;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for changing equipment status (Maintenance/Retire/Available).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatchEquipmentStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;

    private String reason;
}
