package com.nguyenquyen.mockproject_062026_group3.dto.admin.inventory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for consuming stock.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsumeStockRequest {

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    @NotNull(message = "Consumed by (User ID) is required")
    private Long consumedBy;

    private Long residentId; // Optional, for billing linkage

    private String reason;
}
