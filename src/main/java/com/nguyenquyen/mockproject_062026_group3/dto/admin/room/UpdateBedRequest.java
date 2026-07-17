package com.nguyenquyen.mockproject_062026_group3.dto.admin.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating bed info/status.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBedRequest {
    private String bedNumber;
    private String status;
}
