package com.mockproject.group3.dto.admin.incident;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an SLA config.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSlaConfigRequest {
    private Long severityId;
    private Integer slaWindowHrs;
}
