package com.nguyenquyen.mockproject_062026_group3.dto.admin.incident;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new SLA config.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateSlaConfigRequest {

    @NotNull(message = "Severity ID is required")
    private Long severityId;

    @NotNull(message = "SLA window hours is required")
    private Integer slaWindowHrs;
}
