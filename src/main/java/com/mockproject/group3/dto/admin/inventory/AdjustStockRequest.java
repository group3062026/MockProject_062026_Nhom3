package com.mockproject.group3.dto.admin.inventory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adjusting stock (Cycle Count).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdjustStockRequest {

    @NotNull(message = "New stock on hand is required")
    private Integer newStockOnHand;

    @NotNull(message = "Adjusted by (User ID) is required")
    private Long adjustedBy;

    private String reason;
}
