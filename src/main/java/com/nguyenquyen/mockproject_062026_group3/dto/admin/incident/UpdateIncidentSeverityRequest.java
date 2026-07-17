package com.nguyenquyen.mockproject_062026_group3.dto.admin.incident;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a severity level.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateIncidentSeverityRequest {
    private String levelName;
    private Boolean chartLockTrigger;
}
