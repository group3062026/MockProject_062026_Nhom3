package com.nguyenquyen.mockproject_062026_group3.dto.admin.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for assigning equipment to a resident or user.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignEquipmentRequest {
    private Long assignedToResident;
    private Long assignedToUser;
    private String note;
}
