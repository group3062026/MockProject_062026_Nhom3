package com.mockproject.group3.dto.admin.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating supply info.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSupplyRequest {
    private String itemName;
    private Long categoryId;
    private Integer reorderThreshold;
    private BigDecimal unitCost;
    private BigDecimal privatePayRate;
}
