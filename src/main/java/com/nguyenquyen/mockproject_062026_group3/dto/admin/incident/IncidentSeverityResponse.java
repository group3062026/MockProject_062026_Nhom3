package com.nguyenquyen.mockproject_062026_group3.dto.admin.incident;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for incident severity level (AD-08).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IncidentSeverityResponse {
    private Long id;
    private String levelName;
    private Boolean chartLockTrigger;
}
