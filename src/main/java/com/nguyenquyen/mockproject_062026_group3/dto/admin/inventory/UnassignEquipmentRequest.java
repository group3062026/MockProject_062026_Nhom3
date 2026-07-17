package com.nguyenquyen.mockproject_062026_group3.dto.admin.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for unassigning equipment (checkin, return).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnassignEquipmentRequest {
    private String reason;
}
