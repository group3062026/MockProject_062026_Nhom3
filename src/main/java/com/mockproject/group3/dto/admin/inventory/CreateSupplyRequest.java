package com.mockproject.group3.dto.admin.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating new supplies.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateSupplyRequest {

    @NotBlank(message = "Item name is required")
    private String itemName;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Facility ID is required")
    private Long facilityId;

    private Integer initialStock;

    @NotNull(message = "Reorder threshold is required")
    private Integer reorderThreshold;

    @NotNull(message = "Unit cost is required")
    private BigDecimal unitCost;

    @NotNull(message = "Private pay rate is required")
    private BigDecimal privatePayRate;
}
