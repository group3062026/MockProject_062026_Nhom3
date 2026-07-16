package com.mockproject.group3.dto.admin.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for consumable supplies.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupplyResponse {
    private Long id;
    private String itemName;
    private Integer stockOnHand;
    private Integer total;
    private Integer reorderThreshold;
    private BigDecimal unitCost;
    private BigDecimal privatePayRate;
    private String status;
}
